package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import com.yondu.model.PointsRule;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = earnPointsInitWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (!apiResponse.isSuccess()) {
                        showPrompt(apiResponse.getMessage(), "EARN POINTS");
                        enableMenu();
                    } else {
                        loadCustomerDetails();
                        enableMenu();
                    }
                }
            });
            new Thread(task).start();

        });
        pause.play();

    }

    public Task earnPointsInitWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                ApiResponse tempResp = getPointsRule();
                if (tempResp.isSuccess()) {
                    tempResp = App.appContextHolder.memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getMobileNumber());
                    if (tempResp.isSuccess()) {
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage("Network connection error");
                    }
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

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("uuid", customer.getUuid()));
                params.add(new BasicNameValuePair("or_no", orNumber));
                params.add(new BasicNameValuePair("amount", amount.replace(",","")));

                String url = BASE_URL + GIVE_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid",customer.getUuid());
                url = url.replace(":employee_id", employee.getEmployeeId());
                JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);

                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setMessage("Earn points successful.");
                        apiResponse.setSuccess(true);
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
                    if (!apiResponse.isSuccess()) {
                        showPrompt(apiResponse.getMessage(), "EARN POINTS");
                    }
                }
            });
            new Thread(task).start();

        });
        pause.play();
    }

    public ApiResponse getPointsRule() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        String url = BASE_URL + POINTS_CONVERSION_ENDPOINT;
        Employee employee = App.appContextHolder.getEmployee();
        Customer customer = App.appContextHolder.getCustomer();
        url = url.replace(":employee_id", employee.getEmployeeId()).replace(":customer_id", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                PointsRule pointsRule = new PointsRule();
                pointsRule.setEarningPeso((Long) dataJSON.get("earning_peso"));
                pointsRule.setRedeemPeso((Long) dataJSON.get("redemption_peso"));
                App.appContextHolder.setPointsRule(pointsRule);
                apiResponse.setSuccess(true);
            }
        } else {
            apiResponse.setMessage("Network connection error.");
        }

        return apiResponse;
    }
}
