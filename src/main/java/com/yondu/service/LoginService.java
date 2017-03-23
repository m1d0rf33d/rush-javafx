package com.yondu.service;

import com.google.gson.Gson;
import com.yondu.App;
import com.yondu.controller.LoginOnlineController;
import com.yondu.controller.MenuController;
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
import javafx.scene.image.Image;
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
        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {

            Task task = loginInitWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        loadOnline();
                    } else {

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
                            showPrompt(apiResponse.getMessage(), "LOGIN", apiResponse.isSuccess());

                        }

                    } else {
                       if (apiResponse.getMessage().contains("Network"))  {
                           loadOffline();
                       }
                        showPrompt(apiResponse.getMessage(), "LOGIN", apiResponse.isSuccess());

                    }
                App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
            }
        });

        disableMenu();
        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {

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
            scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
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

                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);
                try {
                    Merchant merchant = App.appContextHolder.getMerchant();
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("merchant_type", merchant.getMerchantType());
                    requestBody.put("merchant_key", merchant.getUniqueKey());
                    requestBody.put("employee_login", username);
                    requestBody.put("pin", pin);
                    requestBody.put("branch_id", branch.getId());

                    String url = CMS_URL + LOGIN_EMPLOYEE_ENDPOINT;
                    JSONObject payload = apiService.callWidget(url, requestBody.toJSONString(), "post", merchant.getToken());
                    if (payload != null) {

                        String errorCode = (String) payload.get("error_code");
                        String message = (String) payload.get("message");

                        apiResponse.setMessage(message);
                        apiResponse.setErrorCode(errorCode);
                        if (errorCode.equals("0x0")) {

                            JSONObject data = (JSONObject) payload.get("data");
                            JSONObject employeeJSON = (JSONObject) data.get("employee");
                            Employee employee = new Employee();
                            employee.setEmployeeId((String) employeeJSON.get("id"));
                            employee.setEmployeeName((String)employeeJSON.get("name"));

                            List<String> access = (ArrayList) data.get("access");
                            employee.setScreenAccess(access);
                            App.appContextHolder.setEmployee(employee);
                            App.appContextHolder.setBranch(branch);

                            JSONObject merchantJSON = (JSONObject) data.get("merchant");
                            merchant.setBackgroundUrl((String) merchantJSON.get("background_url"));
                            merchant.setGrayStampsUrl((String)merchantJSON.get("gray_stamps_url"));
                            merchant.setStampsUrl((String)merchantJSON.get("stamps_url"));
                            merchant.setWithVk(branch.getWithVk());

                            if (data.get("titles") != null) {
                                List<JSONObject> titlesJSON = (ArrayList) data.get("titles");
                                List<Title> titles = new ArrayList<>();
                                for (JSONObject json :titlesJSON) {
                                    Title title = new Title();
                                    title.setName((String) json.get("name"));
                                    title.setId((Long) json.get("id"));
                                    titles.add(title);
                                }
                                merchant.setTitles(titles);
                            }

                            apiResponse.setSuccess(true);
                        } else if (errorCode.equals("0x2")){
                            apiResponse.setSuccess(true);
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
                    JSONObject payload = apiService.callWidget(url, null, "get", null);
                    if (payload != null) {
                        JSONObject data = (JSONObject) payload.get("data");
                        List<JSONObject> branchesJSON = (ArrayList) data.get("branches");
                        if (branchesJSON != null) {
                            List<Branch> branches = new ArrayList<>();
                            for (JSONObject branchJSON : branchesJSON) {
                                Branch branch = new Branch();
                                branch.setId((String) branchJSON.get("id"));
                                branch.setName((String) branchJSON.get("name"));
                                branch.setWithVk((Boolean) branchJSON.get("with_vk"));
                                branch.setLogoUrl((String) branchJSON.get("logo_url"));
                                branches.add(branch);
                            }
                            App.appContextHolder.setBranches(branches);

                            JSONObject merchantJSON = (JSONObject) data.get("merchant");
                            Merchant merchant = App.appContextHolder.getMerchant();
                            merchant.setWithVk((Boolean) merchantJSON.get("with_vk"));
                            merchant.setUniqueKey((String) merchantJSON.get("merchant_key"));
                            merchant.setToken((String) merchantJSON.get("token"));
                            merchant.setMerchantType((String) merchantJSON.get("merchant_type"));
                            merchant.setMerchantClassification((String) merchantJSON.get("merchant_class"));
                        }
                        apiResponse.setSuccess(true);
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

    public void reconnectSuccess() {
        Stage stage = (Stage) App.appContextHolder.getRootContainer().getScene().getWindow();

        App.appContextHolder.routeService.goToLoginScreen(stage);
    }

    private void loadOnline() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_ONLINE_FXML));
            Parent root = fxmlLoader.load();
            StackPane bodyStackPane = (StackPane) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            bodyStackPane.getChildren().add(root);
            LoginOnlineController controller = fxmlLoader.getController();
            controller.initAfterLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOffline() {
        try {

            Stage currentStage =(Stage) App.appContextHolder.getRootContainer().getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/menu-offline.fxml"));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));

            Scene scene = stage.getScene();
            scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
            stage.show();
            stage.setMaximized(true);



           /* PauseTransition pause = new PauseTransition(
                    Duration.seconds(.01)
            );
            pause.setOnFinished(event -> {

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
            });
            pause.play();*/
        } catch (IOException e) {
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

            WIDGET_INITIALIZE_ENDPOINT = prop.getProperty("widget_initialize_endpoint");
            LOGIN_EMPLOYEE_ENDPOINT = prop.getProperty("login_employee_endpoint");
            VIEW_MEMBER_ENDPOINT = prop.getProperty("view_member_endpoint");
            REGISTER_MEMBER_ENDPOINT = prop.getProperty("register_member_endpoint");
            EARN_POINTS_ENDPOINT = prop.getProperty("earn_points_endpoint");
            GUEST_PURCHASE_ENDPOINT = prop.getProperty("guest_purchase_endpoint");
            WIDGET_REDEEM_ENDPOINT = prop.getProperty("widget_redeem_endpoint");
            WIDGET_PAY_ENDPOINT = prop.getProperty("widget_pay_endpoint");
            WIDGET_ISSUE_ENDPOINT = prop.getProperty("widget_issue_endpoint");
            SEND_OFFLINE_ENDPOINT = prop.getProperty("send_offline_endpoint");
            EARN_STAMP_ENDPOINT = prop.getProperty("earn_stamps_endpoint");
            REDEEM_STAMP_ENDPOINT = prop.getProperty("redeem_stamps_endpoint");
            ISSUE_STAMP_ENDPOINT = prop.getProperty("issue_stamp_endpoint");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
