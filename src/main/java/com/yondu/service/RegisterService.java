package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.ApiFieldContants;
import javafx.scene.control.Toggle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.REGISTER_ENDPOINT;

/**
 * Created by lynx on 2/21/17.
 */
public class RegisterService {

    private ApiService apiService = new ApiService();

    public ApiResponse register(String name,
                                String email,
                                String mobile,
                                String mpin,
                                LocalDate birthDate,
                                String gender) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, name));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, email));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobile));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, mpin));

        if (birthDate != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
            String str = birthDate.format(dateTimeFormatter);
            params.add(new BasicNameValuePair(ApiFieldContants.BIRTHDATE, str));
        }

        if (gender != null) {
            params.add(new BasicNameValuePair(ApiFieldContants.GENDER, gender));
        }

        String url = BASE_URL + REGISTER_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Registration successful");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }
}
