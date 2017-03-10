package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import com.yondu.model.PointsRule;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;
import static com.yondu.model.constants.AppConfigConstants.REDEEM_REWARDS_SCREEN;

/**
 * Created by erwin on 3/3/2017.
 */
public class PayWithPointsService extends BaseService {

    private EarnPointsService earnPointsService = App.appContextHolder.earnPointsService;
    private ApiService apiService = App.appContextHolder.apiService;

    public void initialize() {

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = initializeWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        PointsRule pointsRule = App.appContextHolder.getPointsRule();
                        Customer customer = App.appContextHolder.getCustomer();
                        loadCustomerDetails();

                        VBox rootVBox = App.appContextHolder.getRootContainer();
                        Label pesoValueLabel = (Label) rootVBox.getScene().lookup("#pesoValueLabel");
                        pesoValueLabel.setText(String.valueOf(Double.parseDouble(customer.getAvailablePoints()) * pointsRule.getRedeemPeso()));
                    } else {
                        showPrompt(apiResponse.getMessage(), "PAY WITH POINTS");
                    }
                    enableMenu();
                }
            });
            new Thread(task).start();

        });
        pause.play();
    }

    private Task initializeWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);


                ApiResponse pointRuleResp = earnPointsService.getPointsRule();
                if (pointRuleResp.isSuccess()) {
                    apiResponse.setSuccess(true);
                    App.appContextHolder.memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getMobileNumber(), App.appContextHolder.getCurrentState());
                } else {
                    apiResponse.setMessage("Network connection error.");
                    return apiResponse;
                }

                return apiResponse;
            }
        };
    }
    public void payWithPoints(String pin) {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = payWithPointsWorker(pin);
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    initialize();
                    clearFields();
                } else {
                    App.appContextHolder.commonService.updateButtonState();
                }
                showPrompt(apiResponse.getMessage(), "PAY WITH POINTS");
                enableMenu();
            });
            new Thread(task).start();
        });
        pause.play();
    }

    private Task payWithPointsWorker(String pin) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                Employee employee = App.appContextHolder.getEmployee();
                Customer customer = App.appContextHolder.getCustomer();

                VBox rootVBox = App.appContextHolder.getRootContainer();
                TextField amountTextField = (TextField) rootVBox.getScene().lookup("#amountTextField");
                TextField receiptTextField = (TextField) rootVBox.getScene().lookup("#receiptTextField");
                TextField pointsTextField = (TextField) rootVBox.getScene().lookup("#pointsTextField");

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("employee_uuid", employee.getEmployeeId()));
                params.add(new BasicNameValuePair("or_no", receiptTextField.getText()));
                params.add(new BasicNameValuePair("amount", amountTextField.getText()));
                params.add(new BasicNameValuePair("points", pointsTextField.getText()));
                params.add(new BasicNameValuePair("pin", pin));

                String url = BASE_URL + PAY_WITH_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid", customer.getUuid());
                JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Pay with points successful.");
                    } else if (jsonObject.get("error_code").equals("0x1")){
                        String message = "";
                        JSONObject errorJSON = (JSONObject) jsonObject.get("errors");
                        if (errorJSON.get("amount") != null) {
                            List arr = (ArrayList) errorJSON.get("amount");
                            message = (String) arr.get(0);
                        }
                        if (errorJSON.get("or_no") != null) {
                            List arr = (ArrayList) errorJSON.get("or_no");
                            message = (String) arr.get(0);
                        }
                        if (errorJSON.get("points") != null) {
                            List arr = (ArrayList) errorJSON.get("points");
                            message = (String) arr.get(0);
                        }
                        apiResponse.setMessage(message);
                    } else if (jsonObject.get("error_code").equals("0x8")) {
                        apiResponse.setMessage((String) jsonObject.get("message"));
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                }
                return apiResponse;
            }
        };
    }

    private void clearFields() {
        VBox rootVBox = App.appContextHolder.getRootContainer();

        TextField amountTextField = (TextField) rootVBox.getScene().lookup("#amountTextField");
        TextField receiptTextField = (TextField) rootVBox.getScene().lookup("#receiptTextField");
        TextField pointsTextField = (TextField) rootVBox.getScene().lookup("#pointsTextField");

        amountTextField.setText(null);
        receiptTextField.setText(null);
        pointsTextField.setText(null);
    }
}
