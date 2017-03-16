package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/21/17.
 */
public class EarnPointsService extends BaseService {

    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void initialize() {

        Task task = earnPointsInitWorker();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (!apiResponse.isSuccess()) {
                    showPrompt(apiResponse.getMessage(), "EARN POINTS");
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    enableMenu();
                } else {
                    loadCustomerDetails();
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    enableMenu();
                }
            }
        });
        new Thread(task).start();

    }

    public Task earnPointsInitWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse resp = App.appContextHolder.memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                if (resp.isSuccess()) {
                    apiResponse.setSuccess(true);

                } else {
                    apiResponse.setMessage("Network connection error");
                }
                return apiResponse;
            }
        };
    }

    public Task earnPointsWorker(String orNumber, String amount) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {

                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Employee employee = App.appContextHolder.getEmployee();
                Customer customer = App.appContextHolder.getCustomer();
                Merchant merchant = App.appContextHolder.getMerchant();

                JSONObject requestBody = new JSONObject();
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("or_no", orNumber);
                requestBody.put("amount", amount);
                requestBody.put("merchant_key",merchant.getUniqueKey());
                requestBody.put("merchant_type", merchant.getMerchantType());

                String token = merchant.getToken();

                String url = CMS_URL + EARN_POINTS_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", token);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setMessage("Earn points successful.");
                        apiResponse.setSuccess(true);
                        JSONObject payload = new JSONObject();
                        payload.put("points", jsonObject.get("points"));
                        apiResponse.setPayload(payload);
                    } else {
                        apiResponse.setMessage((String) jsonObject.get("message"));
                    }
                } else {
                    apiResponse.setMessage("Network error.");
                }
                return apiResponse;
            }
        };
    }

    public void earnPoints(String orNumber, String amount) {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = earnPointsWorker(orNumber, amount);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        String points = (String) apiResponse.getPayload().get("points");
                        VBox vBox = App.appContextHolder.getRootContainer();
                        Label pointsLabel = (Label) vBox.getScene().lookup("#pointsLabel");
                        pointsLabel.setText(points);
                        clearFields();

                        Customer customer = App.appContextHolder.getCustomer();
                        Employee employee = App.appContextHolder.getEmployee();
                        saveTransaction(TransactionType.EARN_POINTS,
                                customer.getMobileNumber(),
                                employee.getEmployeeName(),
                                amount,orNumber, null);
                    }
                    showPrompt(apiResponse.getMessage(), "EARN POINTS");
                    enableMenu();
                }
            });
            task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    showPrompt("Network connection error.", "EARN POINTS");
                    enableMenu();
                }
            });
            new Thread(task).start();

        });
        pause.play();
    }

    private void clearFields() {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField receiptTextField = (TextField) rootVBox.getScene().lookup("#receiptTextField");
        TextField amountTextField = (TextField) rootVBox.getScene().lookup("#amountTextField");
        TextField pointsTextField = (TextField) rootVBox.getScene().lookup("#pointsTextField");
        receiptTextField.setText(null);
        amountTextField.setText(null);
        pointsTextField.setText(null);

    }
}
