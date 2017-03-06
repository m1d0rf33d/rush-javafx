package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Transaction;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/21/17.
 */
public class TransactionService extends BaseService {

    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void initialize() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = initializeWorker();
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    loadCustomerDetails();
                    renderTransactionTable();
                }
                enableMenu();
            });
            new Thread(task).start();
        });
        pause.play();
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {

                ApiResponse apiResponse = new ApiResponse();

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse loginResp = memberDetailsService.loginCustomer(customer.getMobileNumber());
                if (loginResp.isSuccess()) {
                    ApiResponse transactionsResp = getTransactions();
                    if (transactionsResp.isSuccess()) {
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage("Network connection error.");
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                    return apiResponse;
                }
                return apiResponse;
            }
        };
    }

    public ApiResponse getTransactions() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List params = new ArrayList<>();
        String url = BASE_URL + CUSTOMER_TRANSACTION_ENDPOINT;

        Customer customer = App.appContextHolder.getCustomer();
        url = url.replace(":customer_uuid", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, params, "get", CUSTOMER_APP_RESOUCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> data = (ArrayList) jsonObject.get("data");

                List<Transaction> transactions = new ArrayList<>();
                for (JSONObject json : data) {
                    Transaction transaction = new Transaction();
                    transaction.setReceiptNumber((String) json.get("receipt_no"));
                    transaction.setTransactionType((String) json.get("transaction_type"));
                    transaction.setPointsEarned((String) json.get("points_earned"));
                    transaction.setCashPaid(String.valueOf(json.get("amount_paid_with_cash")));
                    transaction.setPointsPaid(String.valueOf(json.get("amount_paid_with_points")));
                    transaction.setDate((String) json.get("date"));
                    transactions.add(transaction);
                }
                customer.setTransactions(transactions);
                apiResponse.setSuccess(true);
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }

    public void renderTransactionTable() {

    }
}
