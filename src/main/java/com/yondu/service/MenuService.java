package com.yondu.service;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.yondu.AppContextHolder.*;

/**
 * Created by lynx on 2/9/17.
 */
public class MenuService {

    private ApiService apiService = new ApiService();

    public JSONObject loginCustomer(String mobileNumber) {
        JSONObject responseJSON = new JSONObject();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNumber));
        String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                Customer customer = new Customer();
                customer.setMobileNumber((String) data.get("mobile_no"));
                customer.setGender((String) data.get("gender"));
                customer.setMemberId((String) data.get("profile_id"));
                customer.setName((String) data.get("name"));
                customer.setDateOfBirth((String) data.get("birthdate"));
                customer.setEmail((String) data.get("email"));
                customer.setMemberSince((String) data.get("registration_date"));
                responseJSON.put("customer", customer);

                App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                App.appContextHolder.setCustomerUUID((String) data.get("id"));

                url = BASE_URL + GET_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                jsonObject = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                String points = (String) jsonObject.get("data");
                customer.setAvailablePoints(points);

                url = BASE_URL + CUSTOMER_REWARDS_ENDPOINT;
                url = url.replace(":id",App.appContextHolder.getCustomerUUID());
                jsonObject = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                List<Reward> rewards = new ArrayList<>();
                for (JSONObject rewardJSON : dataJSON) {
                    Reward reward = new Reward();
                    reward.setDetails((String) rewardJSON.get("details"));
                    reward.setName((String) rewardJSON.get("name"));
                    reward.setQuantity(((Long) rewardJSON.get("quantity")).toString());
                    rewards.add(reward);
                }
                customer.setActiveVouchers(rewards);
            } else {
                responseJSON.put("message", jsonObject.get("message"));
            }

        }

        return responseJSON;
    }

    public JSONObject getRewards() {
        JSONObject responseJSON = new JSONObject();
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

                rewards.add(reward);
            }
            responseJSON.put("rewards", rewards);
        }

        return responseJSON;
    }
}
