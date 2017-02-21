package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.PointsRule;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.GIVE_POINTS_ENDPOINT;
import static com.yondu.AppContextHolder.POINTS_CONVERSION_ENDPOINT;

/**
 * Created by lynx on 2/21/17.
 */
public class EarnPointsService {

    private ApiService apiService = new ApiService();

    public ApiResponse earnPoints(String orNumber, String amount) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        JSONObject resultJSON = new JSONObject();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
        String url = BASE_URL + GIVE_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

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

    public ApiResponse getPointsRule() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        JSONObject payload = new JSONObject();

        String url = BASE_URL + POINTS_CONVERSION_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject dataJSON = (JSONObject) jsonObject.get("data");
                PointsRule pointsRule = new PointsRule();
                pointsRule.setEarningPeso((Long) dataJSON.get("earning_peso"));
                pointsRule.setRedeemPeso((Long) dataJSON.get("redemption_peso"));
                payload.put("pointsRule", pointsRule);
                apiResponse.setPayload(payload);
                apiResponse.setSuccess(true);
            }
        } else {
            apiResponse.setMessage("Network error");
        }

        return apiResponse;
    }
}
