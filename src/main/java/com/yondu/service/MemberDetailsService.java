package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.*;

/**
 * Created by lynx on 2/21/17.
 */
public class MemberDetailsService {

    private ApiService apiService = new ApiService();

    public ApiResponse loginCustomer(String mobileNumber) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        JSONObject payload = new JSONObject();

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
                payload.put("customer", customer);

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
                    reward.setQuantity((rewardJSON.get("quantity")).toString());
                    reward.setId(String.valueOf((Long) rewardJSON.get("id")));
                    reward.setImageUrl((String) rewardJSON.get("image_url"));
                    reward.setDate((String) rewardJSON.get("date"));
                    reward.setPointsRequired(String.valueOf((Long) rewardJSON.get("points")));
                    rewards.add(reward);
                }
                customer.setActiveVouchers(rewards);
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Redeem reward successful.");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }

        }else {
            apiResponse.setMessage("Network error.");
        }

        apiResponse.setPayload(payload);
        return apiResponse;
    }

    public ApiResponse getCurrentPoints() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        JSONObject payload = new JSONObject();

        List<NameValuePair> params = new ArrayList<>();
        String url = BASE_URL + GET_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            payload.put("points", jsonObject.get("data"));
            apiResponse.setSuccess(true);
            apiResponse.setPayload(payload);
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }

    public ApiResponse getActiveVouchers() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        String url = BASE_URL + CUSTOMER_REWARDS_ENDPOINT;
        url = url.replace(":id",App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                List<Reward> rewards = new ArrayList<>();
                for (JSONObject rewardJSON : dataJSON) {
                    Reward reward = new Reward();
                    reward.setDetails((String) rewardJSON.get("details"));
                    reward.setName((String) rewardJSON.get("name"));
                    reward.setQuantity((rewardJSON.get("quantity")).toString());
                    reward.setId(String.valueOf((Long) rewardJSON.get("id")));
                    reward.setImageUrl((String) rewardJSON.get("image_url"));
                    rewards.add(reward);
                }
                JSONObject payload = new JSONObject();
                payload.put("rewards", rewards);
                apiResponse.setPayload(payload);
                apiResponse.setSuccess(true);
            }
        }
        return apiResponse;
    }

    public ApiResponse loginEmployee(String employeeId, String branchId, String pin) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, employeeId));
        params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, branchId));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
        String url = BASE_URL + LOGIN_ENDPOINT;
        JSONObject jsonObject = apiService.call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");

                JSONObject payload = new JSONObject();
                Employee employee = new Employee();
                employee.setBranchId(branchId);
                employee.setEmployeeId((String) data.get("id"));
                employee.setEmployeeName((String) data.get("name"));
                payload.put("employee", employee);
                apiResponse.setSuccess(true);
                apiResponse.setPayload(payload);
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network error");
        }
        return apiResponse;
    }

}
