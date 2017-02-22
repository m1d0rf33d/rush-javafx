package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import  static com.yondu.AppContextHolder.*;
/**
 * Created by lynx on 2/22/17.
 */
public class GuestPurchaseService {

    private ApiService apiService = new ApiService();

    public ApiResponse givePoints(String mobileNo, String orNumber, String amount) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNo));
        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
        String url =BASE_URL +  EARN_GUEST_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Earn points successful.");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }
}
