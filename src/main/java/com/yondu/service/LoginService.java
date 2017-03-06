package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.LoginOnlineController;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Employee;
import com.yondu.model.constants.AppConfigConstants;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
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
                        commonService.showPrompt("Network connection error.", "LOGIN");
                        loadOffline();
                    }
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
                        Stage currentStage = ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow());

                        App.appContextHolder.routeService.goToMenuScreen(currentStage);
                    } else {
                        if (apiResponse.getErrorCode().equals("0x2")) {
                            showPinDialog();
                        } else {
                            showPrompt(apiResponse.getMessage(), "LOGIN");
                        }
                    }
                    enableMenu();
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

    private void showPinDialog() {
        try {

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 500,300);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
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

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("employee_id", username));
                params.add(new BasicNameValuePair("branch_id", branch.getId()));
                if (pin != null) {
                    params.add(new BasicNameValuePair("pin", pin));
                }

                String url = BASE_URL + LOGIN_ENDPOINT;
                JSONObject jsonObject = apiService.call((url), params, "post", MERCHANT_APP_RESOURCE_OWNER);
                if (jsonObject != null) {
                    apiResponse.setErrorCode((String) jsonObject.get("error_code"));
                    if (jsonObject.get("error_code").equals("0x0")) {
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        Employee employee = new Employee();
                        employee.setEmployeeId((String) data.get("id"));
                        employee.setEmployeeName((String) data.get("name"));

                        App.appContextHolder.setBranch(branch);
                        App.appContextHolder.setEmployee(employee);
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage((String) jsonObject.get("message"));
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
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

                loadWidgetEndpoints();
                if (commonService.fetchApiKeys()) {
                    loadRushEndpoints();
                    loadMerchantBranches();
                    apiResponse.setSuccess(true);
                } else {
                    apiResponse.setSuccess(false);
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
            String merchantType = MERCHANT_TYPE.toString().toLowerCase();
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
                } else {
                    showPrompt(apiResponse.getMessage(), "LOGIN");
                }
                enableMenu();
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

                if (commonService.fetchApiKeys()) {
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
                            branches.add(branch);
                        }
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
