package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import com.yondu.model.constants.AppState;
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

        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
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
                        showPrompt(apiResponse.getMessage(), "PAY WITH POINTS",apiResponse.isSuccess());
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

                String mobileNumber = App.appContextHolder.getCustomer().getMobileNumber();
                AppState appState = App.appContextHolder.getCurrentState();

                return App.appContextHolder.memberDetailsService.loginCustomer(mobileNumber, appState);

            }
        };
    }
    public void payWithPoints(String pin) {
        disableMenu();
        showLoadingScreen();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = payWithPointsWorker(pin);
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    Customer customer = App.appContextHolder.getCustomer();
                    PointsRule pointsRule = App.appContextHolder.getPointsRule();
                    VBox rootVBox = App.appContextHolder.getRootContainer();
                    Label pointsLabel = (Label) rootVBox.getScene().lookup("#pointsLabel");
                    pointsLabel.setText(customer.getAvailablePoints());
                    Label pesoValueLabel = (Label) rootVBox.getScene().lookup("#pesoValueLabel");
                    pesoValueLabel.setText(String.valueOf(Double.parseDouble(customer.getAvailablePoints()) * pointsRule.getRedeemPeso()));

                    clearFields();
                } else {
                    App.appContextHolder.commonService.updateButtonState();
                }
                hideLoadingScreen();
                showPrompt(apiResponse.getMessage(), "PAY WITH POINTS", apiResponse.isSuccess());

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
                Merchant merchant = App.appContextHolder.getMerchant();

                VBox rootVBox = App.appContextHolder.getRootContainer();
                TextField amountTextField = (TextField) rootVBox.getScene().lookup("#amountTextField");
                TextField receiptTextField = (TextField) rootVBox.getScene().lookup("#receiptTextField");
                TextField pointsTextField = (TextField) rootVBox.getScene().lookup("#pointsTextField");

                JSONObject requestBody = new JSONObject();
                requestBody.put("merchant_type", merchant.getMerchantType());
                requestBody.put("merchant_key", merchant.getUniqueKey());
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("points", pointsTextField.getText());
                requestBody.put("amount", amountTextField.getText());
                requestBody.put("or_no", receiptTextField.getText());
                requestBody.put("pin", pin);
                requestBody.put("employee_id", employee.getEmployeeId());

                String token = merchant.getToken();

                String url = CMS_URL + WIDGET_PAY_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", token);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Pay with points successful.");

                        JSONObject data = (JSONObject) jsonObject.get("data");
                        String points = (String) data.get("points");
                        customer.setAvailablePoints(points);
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
                        saveTransaction(TransactionType.PAY_WITH_POINTS, customer.getMobileNumber(), employee.getEmployeeName(), amountTextField.getText(), receiptTextField.getText(), null);
                    } else  {
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
