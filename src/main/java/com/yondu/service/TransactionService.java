package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Transaction;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.CUSTOMER_TRANSACTION_ENDPOINT;
import static com.yondu.model.constants.ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER;

/**
 * Created by lynx on 2/21/17.
 */
public class TransactionService {

    private ApiService apiService = new ApiService();

    public ApiResponse getTransactions() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List params = new ArrayList<>();
        String url = BASE_URL + CUSTOMER_TRANSACTION_ENDPOINT;
        url = url.replace(":customer_uuid", App.appContextHolder.getCustomerUUID());
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
                    transaction.setCashPaid(String.valueOf((Long) json.get("amount_paid_with_cash")));
                    transaction.setPointsPaid(String.valueOf((Long) json.get("amount_paid_with_points")));
                    transaction.setDate((String) json.get("date"));
                    transactions.add(transaction);
                }
                JSONObject payload = new JSONObject();
                payload.put("transactions", transactions);
                apiResponse.setPayload(payload);
                apiResponse.setSuccess(true);
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }
}
