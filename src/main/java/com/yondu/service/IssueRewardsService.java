package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.CLAIM_REWARDS_ENDPOINT;

/**
 * Created by lynx on 2/21/17.
 */
public class IssueRewardsService {

    private ApiService apiService = new ApiService();

    public ApiResponse issueReward(String redeemId) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.REDEEM_ID, redeemId));
        String url = BASE_URL + CLAIM_REWARDS_ENDPOINT;
        url = url.replace(":customer_id", App.appContextHolder.getCustomerUUID());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            apiResponse.setMessage("Issue reward successful.");
            apiResponse.setSuccess(true);
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }
}
