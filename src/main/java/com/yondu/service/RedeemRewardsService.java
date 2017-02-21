package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.PinController;
import com.yondu.model.ApiResponse;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.GET_REWARDS_MERCHANT_ENDPOINT;
import static com.yondu.AppContextHolder.REDEEM_REWARDS_ENDPOINT;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.PIN_SCREEN;

/**
 * Created by lynx on 2/21/17.
 */
public class RedeemRewardsService {

    private ApiService apiService = new ApiService();

    public ApiResponse redeemRewards(String pin, String rewardId) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
        String url = BASE_URL + REDEEM_REWARDS_ENDPOINT;
        url = url.replace(":customer_id",App.appContextHolder.getCustomerUUID());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":reward_id", rewardId);
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Redeem reward successful.");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }


    public ApiResponse getRewards() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        JSONObject payload = new JSONObject();
        String url = BASE_URL + GET_REWARDS_MERCHANT_ENDPOINT;
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            List<Reward> rewards = new ArrayList<>();
            List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
            for (JSONObject rewardJSON : dataJSON) {
                Reward reward = new Reward();
                reward.setImageUrl((String) rewardJSON.get("image_url"));
                reward.setDetails((String) rewardJSON.get("details"));
                reward.setName((String) rewardJSON.get("name"));
                reward.setId((String) rewardJSON.get("id"));
                reward.setPointsRequired(String.valueOf((Long) rewardJSON.get("points_required")));
                rewards.add(reward);
            }
            payload.put("rewards", rewards);
            apiResponse.setSuccess(true);
            apiResponse.setPayload(payload);
        } else {
            apiResponse.setMessage("Network error.");
        }

        return apiResponse;
    }
}
