package com.yondu.service;

import com.google.gson.Gson;
import com.yondu.App;
import com.yondu.controller.LoginOnlineController;
import com.yondu.model.*;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.dto.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.yondu.model.constants.ApiConstants.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by erwin on 3/1/2017.
 */
public class LoginService extends BaseService{

    private CommonService commonService = App.appContextHolder.commonService;
    private ApiService apiService       = App.appContextHolder.apiService;
    private RouteService routeService = App.appContextHolder.routeService;

    public void initialize() {
        disableMenu();

        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
            Task task = loginInitWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        loadOnline();
                    } else {
                        commonService.showPrompt("Network connection error.", "LOGIN");
                        loadOffline();
                    }

                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    enableMenu();
                }
            });
            disableMenu();
            new Thread(task).start();
        });
        pause.play();
    }

    public void loginEmployee(String username, Branch branch, String pin) {

        Task task = loginEmployeeWorker(username, branch, pin);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        if (apiResponse.getErrorCode().equals("0x0")) {
                            Stage currentStage = ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow());
                            App.appContextHolder.routeService.goToMenuScreen(currentStage);
                        } else if (apiResponse.getErrorCode().equals("0x2")){
                            showPinDialog();
                        } else {
                            showPrompt(apiResponse.getMessage(), "LOGIN");
                            enableMenu();
                        }

                    } else {
                       if (apiResponse.getMessage().contains("Network"))  {
                           loadOffline();
                       }
                        showPrompt(apiResponse.getMessage(), "LOGIN");
                        enableMenu();
                    }
                App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
            }
        });

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {
            App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
            new Thread(task).start();
        });
        pause.play();

    }

    private void showPinDialog() {
        try {

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 420,220);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.resizableProperty().setValue(Boolean.FALSE);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    enableMenu();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Task loginEmployeeWorker(String username, Branch branch, String pin) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {

                Merchant merchant = App.appContextHolder.getMerchant();

                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);
                try {

                    LoginDTO loginDTO = new LoginDTO();
                    loginDTO.setBranchId(branch.getId());
                    loginDTO.setEmployeeId(username);
                    loginDTO.setPin(pin);
                    loginDTO.setMerchantKey(merchant.getUniqueKey());

                    Gson gson = new Gson();
                    String payload = gson.toJson(loginDTO);

                    String url = CMS_URL + LOGIN_EMPLOYEE_ENDPOINT;
                    JSONObject jsonObject = apiService.callWidget(url, payload, "post", merchant.getToken());
                    if (jsonObject != null) {
                        WidgetResponse<LoginResponseDTO> widgetResponse = gson.fromJson(jsonObject.toJSONString(), WidgetResponse.class);
                        apiResponse.setErrorCode(widgetResponse.getErrorCode());
                        if (widgetResponse.getErrorCode().equals("0x0")) {
                            String str = gson.toJson(widgetResponse.getData());
                            LoginResponseDTO loginResponseDTO = gson.fromJson(str, LoginResponseDTO.class);
                            EmployeeDTO employeeDTO = loginResponseDTO.getEmployeeDTO();
                            List<String> screenAccess = loginResponseDTO.getScreenAccess();
                            Employee employee = new Employee();
                            employee.setEmployeeId(employeeDTO.getId());
                            employee.setEmployeeName(employeeDTO.getName());
                            employee.setScreenAccess(screenAccess);
                            App.appContextHolder.setEmployee(employee);
                            App.appContextHolder.setBranch(branch);

                            MerchantDTO merchantDTO = loginResponseDTO.getMerchantDTO();
                            merchant.setBackgroundUrl(merchantDTO.getBackgroundUrl());
                            merchant.setGrayStampsUrl(merchantDTO.getGrayStampsUrl());
                            merchant.setStampsUrl(merchantDTO.getStampsUrl());

                            apiResponse.setSuccess(true);
                        } else if (widgetResponse.getErrorCode().equals("0x2")){
                            apiResponse.setSuccess(true);
                        } else {
                            apiResponse.setMessage(widgetResponse.getMessage());
                            apiResponse.setSuccess(false);
                        }

                    } else {
                        apiResponse.setMessage("Network connection error.");
                        apiResponse.setSuccess(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return apiResponse;
            }
        };
    }

    public Task loginInitWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();

              try {
                  loadWidgetEndpoints();
                  loadMerchantDetails();

                  String merchantKey = App.appContextHolder.getMerchant().getUniqueKey();
                  String url = CMS_URL + WIDGET_INITIALIZE_ENDPOINT.replace(":merchantKey", merchantKey);
                  JSONObject jsonObject = apiService.callWidget(url, null, "get", null);
                  if (jsonObject != null) {
                      Gson gson = new Gson();
                      WidgetResponse<WidgetInitDTO> widgetResponse = gson.fromJson(jsonObject.toJSONString(), WidgetResponse.class);
                      String str = gson.toJson(widgetResponse.getData());
                      WidgetInitDTO widgetInitDTO = gson.fromJson(str, WidgetInitDTO.class);
                      List<Branch> branches = new ArrayList<>();
                      List<BranchDTO> branchDTOs = widgetInitDTO.getBranchDTOs();
                      for (BranchDTO branchDTO : branchDTOs) {
                          Branch branch = new Branch();
                          branch.setId(branchDTO.getUuid());
                          branch.setName(branchDTO.getBranchName());
                          branch.setLogoUrl(branchDTO.getLogoUrl());
                          branches.add(branch);
                      }
                      App.appContextHolder.setBranches(branches);

                      MerchantDTO merchantDTO = widgetInitDTO.getMerchantDTO();
                      Merchant merchant = App.appContextHolder.getMerchant();
                      merchant.setWithVk(merchantDTO.getWithVk());
                      merchant.setToken(merchantDTO.getToken());
                      merchant.setMerchantType(merchantDTO.getMerchantType());
                      apiResponse.setSuccess(true);

                      loadRushEndpoints();
                  } else {
                      apiResponse.setMessage("Network connection error");
                      apiResponse.setSuccess(false);
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
                return apiResponse;
            }
        };
    }

    private void loadMerchantBranches() {
        JSONArray branches = new JSONArray();

        String url = BASE_URL + GET_BRANCHES_ENDPOINT;
        java.util.List<NameValuePair> params = new ArrayList<>();
        JSONObject jsonObject = apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            List<JSONObject> data = (ArrayList) jsonObject.get("data");
            for (JSONObject json : data) {
                Branch branch = new Branch();
                branch.setId((String) json.get("id"));
                branch.setName((String) json.get("name"));
                branch.setLogoUrl((String) json.get("logo_url"));
                branches.add(branch);
            }
            App.appContextHolder.setBranches(branches);
        } else {
            loadOffline();
        }
    }

    private void loadOnline() {
        try {
            VBox numbersVBox = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#numbersVBox");
            numbersVBox.setVisible(true);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_ONLINE_FXML));
            Parent root = fxmlLoader.load();
            LoginOnlineController controller = fxmlLoader.getController();
            StackPane bodyStackPane = (StackPane) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOffline() {
        try {
            Text text = new Text("Network connection error.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
            alert.setHeaderText("LOGIN");
            alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();

            VBox numbersVBox = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#numbersVBox");
            StackPane bodyStackPane = (StackPane) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            numbersVBox.setVisible(false);
            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfigConstants.LOGIN_OFFLINE_FXML));
            Parent root = fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRushEndpoints() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(API_PROPERTIES);
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
            } else {
                throw new FileNotFoundException("property file api.properties not found in the classpath");
            }
            Merchant merchant = App.appContextHolder.getMerchant();
            String merchantType = merchant.getMerchantType().toLowerCase();
            BASE_URL = prop.getProperty("base_url").replace(":merchant_type", merchantType);
            REGISTER_ENDPOINT = prop.getProperty("register_endpoint").replace(":merchant_type", merchantType);
            MEMBER_LOGIN_ENDPOINT = prop.getProperty("member_login_endpoint").replace(":merchant_type", merchantType);
            POINTS_CONVERSION_ENDPOINT = prop.getProperty("points_conversion_endpoint").replace(":merchant_type", merchantType);
            GIVE_POINTS_ENDPOINT = prop.getProperty("give_points_endpoint").replace(":merchant_type", merchantType);
            GET_POINTS_ENDPOINT = prop.getProperty("get_points_endpoint").replace(":merchant_type", merchantType);
            PAY_WITH_POINTS_ENDPOINT = prop.getProperty("pay_points_endpoint").replace(":merchant_type", merchantType);
            GET_REWARDS_ENDPOINT = prop.getProperty("get_rewards_endpoint").replace(":merchant_type", merchantType);
            REDEEM_REWARDS_ENDPOINT = prop.getProperty("redeem_rewards_endpoint").replace(":merchant_type", merchantType);
            UNCLAIMED_REWARDS_ENDPOINT = prop.getProperty("unclaimed_rewards_endpoint").replace(":merchant_type", merchantType);
            CLAIM_REWARDS_ENDPOINT = prop.getProperty("claim_rewards_endpoint").replace(":merchant_type", merchantType);
            GET_REWARDS_MERCHANT_ENDPOINT = prop.getProperty("get_rewards_merchant_endpoint").replace(":merchant_type", merchantType);
            CUSTOMER_REWARDS_ENDPOINT = prop.getProperty("customer_rewards_endpoint").replace(":merchant_type", merchantType);
            CUSTOMER_TRANSACTION_ENDPOINT = prop.getProperty("customer_transactions_endpoint").replace(":merchant_type", merchantType);
            GET_BRANCHES_ENDPOINT = prop.getProperty("get_branches_endpoint").replace(":merchant_type", merchantType);
            LOGIN_ENDPOINT = prop.getProperty("login_endpoint").replace(":merchant_type", merchantType);
            AUTHORIZATION_ENDPOINT = prop.getProperty("authorization_endpoint").replace(":merchant_type", merchantType);
            MERCHANT_DESIGNS_ENDPOINT = prop.getProperty("merchant_designs_endpoint").replace(":merchant_type", merchantType);
            MERCHANT_SETTINGS_ENDPOINT = prop.getProperty("merchant_settings_endpoint").replace(":merchant_type", merchantType);
            EARN_GUEST_ENDPOINT = prop.getProperty("earn_guest_endpoint").replace(":merchant_type", merchantType);
            CUSTOMER_CARD_ENDPOINT = prop.getProperty("customer_card_endpoint").replace(":merchant_type", merchantType);
            EARN_STAMPS_ENDPOINT = prop.getProperty("earn_stamps_endpoint").replace(":merchant_type", merchantType);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void loadMerchantDetails() {
      try {
          File file = new File(RUSH_HOME + DIVIDER + AppConfigConstants.ACTIVATION_FILE);
          if (file.exists()) {
              BufferedReader br = new BufferedReader(new FileReader(file));
              String l = "";
              String merchantKey = null;
              while ((l = br.readLine()) != null) {
                  String[] arr = l.split("=");
                  merchantKey = arr[1];
              }
              br.close();
              Merchant merchant = new Merchant();
              merchant.setUniqueKey(merchantKey);
              App.appContextHolder.setMerchant(merchant);
          }
      } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    private void loadWidgetEndpoints() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(API_PROPERTIES);
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
            } else {
                throw new FileNotFoundException("property file api.properties not found in the classpath");
            }

            CMS_URL = prop.getProperty("cms_url");
            TOMCAT_PORT = prop.getProperty("tomcat_port");
            OAUTH_SECRET = prop.getProperty("oauth_secret");
            OAUTH_ENDPOINT = prop.getProperty("oauth_endpoint");
            VALIDATE_MERCHANT_ENDPOINT = prop.getProperty("validate_merchant_endpoint");
            ACCESS_ENDPOINT = prop.getProperty("access_endpoint");

            WIDGET_INITIALIZE_ENDPOINT = prop.getProperty("widget_initialize_endpoint");
            LOGIN_EMPLOYEE_ENDPOINT = prop.getProperty("login_employee_endpoint");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void reconnect() {
        Task task = reconnectWorker();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {

                    loadOnline();
                    enableMenu();
                } else {
                    showPrompt(apiResponse.getMessage(), "LOGIN");
                    enableMenu();
                }

            }
        });

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {
            new Thread(task).start();
        });
        pause.play();
    }

    public Task reconnectWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                if (App.appContextHolder.commonService.fetchApiKeys()) {
                    loadRushEndpoints();

                    JSONArray branches = new JSONArray();
                    String url = BASE_URL + GET_BRANCHES_ENDPOINT;
                    java.util.List<NameValuePair> params = new ArrayList<>();
                    JSONObject jsonObject = App.appContextHolder.apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
                    if (jsonObject != null) {
                        List<JSONObject> data = (ArrayList) jsonObject.get("data");
                        for (JSONObject json : data) {
                            Branch branch = new Branch();
                            branch.setId((String) json.get("id"));
                            branch.setName((String) json.get("name"));
                            branch.setLogoUrl((String) json.get("logo_url"));
                            branches.add(branch);
                        }
                        App.appContextHolder.setBranches(branches);
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage("Network connection error.");
                        return apiResponse;
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                    return apiResponse;
                }

                return apiResponse;
            }
        };
    }
}
