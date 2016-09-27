package com.yondu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.ApiFieldContants;
import com.yondu.model.ApiResponse;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.scene.web.WebEngine;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.html.HTMLInputElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;

/** Home Module services / Java2Javascript bridge
 *  Methods inside this class can be invoked inside a javascript using alert("__CONNECT__BACKEND__homeService")
 *
 *  @author m1d0rf33d
 */
public class HomeService {

    private WebEngine webEngine;
    private ApiService apiService = new ApiService();

    private String baseUrl;
    private String registerEndpoint;
    private String memberLoginEndpoint;
    private String pointsConversionEndpoint;
    private String givePointsEndpoint;
    private String getPointsEndpoint;
    private String payWithPointsEndpoint;
    private String getRewardsEndpoint;
    private String redeemRewardsEndpoint;
    private String unclaimedRewardsEndpoint;
    private String claimRewardsEndpoint;

    public HomeService(WebEngine webEngine) {
        this.webEngine = webEngine;
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file api.properties not found in the classpath");
            }
            this.baseUrl = prop.getProperty("base_url");
            this.registerEndpoint = prop.getProperty("register_endpoint");
            this.memberLoginEndpoint = prop.getProperty("member_login_endpoint");
            this.pointsConversionEndpoint = prop.getProperty("points_conversion_endpoint");
            this.givePointsEndpoint = prop.getProperty("give_points_endpoint");
            this.getPointsEndpoint = prop.getProperty("get_points_endpoint");
            this.payWithPointsEndpoint = prop.getProperty("pay_points_endpoint");
            this.getRewardsEndpoint = prop.getProperty("get_rewards_endpoint");
            this.redeemRewardsEndpoint = prop.getProperty("redeem_rewards_endpoint");
            this.unclaimedRewardsEndpoint = prop.getProperty("unclaimed_rewards_endpoint");
            this.claimRewardsEndpoint = prop.getProperty("claim_rewards_endpoint");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEmployeeData(final Object callbackfunction) {

        Account account = new Account();
        account.setId(App.appContextHolder.getEmployeeId());
        account.setName(App.appContextHolder.getEmployeeName());

        ObjectMapper mapper = new ObjectMapper();
        String data = null;
        try {
            data = mapper.writeValueAsString(account);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        final String jsonData =  data;
        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, jsonData);
        }).start();
    }

    public void register() {
        //Read html form values
        HTMLInputElement nameField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_NAME);
        HTMLInputElement emailField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_EMAIL);
        HTMLInputElement mobileField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_MOBILE);
        HTMLInputElement pinField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_PIN);

        String jsonResponse = "";
        //Validate fields
        if (nameField.getValue() == null || emailField.getValue() == null
                || mobileField.getValue() == null || pinField.getValue() == null) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setMessage("Please fill up all fields.");
            ObjectMapper mapper = new ObjectMapper();
            try {
                jsonResponse = mapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, nameField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, emailField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_PIN, pinField.getValue()));
            String url = baseUrl + registerEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
            jsonResponse = apiService.call(url, params, "post");
        }
        this.webEngine.executeScript("registerResponseHandler('"+jsonResponse+"')");
    }

    public void loginMember(final Object callbackfunction) {
        //Read html form values
        HTMLInputElement mobileField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_MOBILE);

        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getValue()));

        String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
        String result = apiService.call(url, params, "post");

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(result);
            String error =  (String) jsonObject.get("error_code");
            if (error.equals("0x0")) {
                JSONObject data =  (JSONObject) jsonObject.get("data");
                App.appContextHolder.setCustomerId( (String) data.get("id"));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, result);
        }
        ).start();
    }

    public void loadPointsRule(final Object callbackfunction) {

        String url = baseUrl + pointsConversionEndpoint;
        String result = apiService.call(url, new ArrayList<>(), "get");

        new Thread( () -> {
            try {
                sleep(5000); //add some processing simulation...
                runLater( () ->
                        Java2JavascriptUtils.call(callbackfunction, result)
                );
            } catch (InterruptedException e) {	}

        }).start();
    }
    public void givePointsToCustomer() {
        //Read html form values
        HTMLInputElement amountField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.AMOUNT);
        HTMLInputElement orNumberField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.OR_NUMBER);

        String jsonResponse = "";
        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumberField.getValue()));
        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amountField.getValue()));
        String url = baseUrl + givePointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerId());
        jsonResponse = apiService.call(url, params, "post");
        this.webEngine.executeScript("givePointsResponseHandler('"+jsonResponse+"')");
    }

    public void getPoints() {
        String jsonResponse = "";
        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        String url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerId());
        jsonResponse = apiService.call(url, params, "get");
        this.webEngine.executeScript("getPointsHandler('"+jsonResponse+"')");
    }

    public void payWithPoints() {
        //Read html form values
        HTMLInputElement pointsField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.POINTS);
        HTMLInputElement orNumberField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.OR_NUMBER);
        HTMLInputElement amountField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.AMOUNT);

        String jsonResponse = "";
        //Validate fields
        if (pointsField.getValue() == null || orNumberField.getValue() == null
                || amountField.getValue() == null) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setMessage("Please fill up all fields.");
            ObjectMapper mapper = new ObjectMapper();
            try {
                jsonResponse = mapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
            params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumberField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amountField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.POINTS, pointsField.getValue()));
            String url = baseUrl + payWithPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerId());
            jsonResponse = apiService.call(url, params, "post");
        }
        this.webEngine.executeScript("givePointsResponseHandler('"+jsonResponse+"')");
    }

    public void loadRewards(final Object callbackfunction) {


        String url = baseUrl + getRewardsEndpoint;
        String jsonResponse = apiService.call(url, new ArrayList<>(), "get");

        final String data = jsonResponse;
        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, data);
        }).start();
    }

    public void redeemRewards(String rewardId, String pin) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
        String url = baseUrl + redeemRewardsEndpoint.replace(":customer_id",App.appContextHolder.getCustomerId());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":reward_id", rewardId);
        String jsonResponse = apiService.call(url, params, "post");

        this.webEngine.executeScript("redeemRewardsResponseHandler('"+jsonResponse+"')");
    }

    public void loadCustomerRewards(final Object callbackfunction) {


        String url = baseUrl + unclaimedRewardsEndpoint;
        String jsonResponse = apiService.call(url, new ArrayList<>(), "get");

        final String data = jsonResponse;
        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, data);
        }).start();
    }


    public void issueRewards(String rewardId, String pin) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
        String url = baseUrl + claimRewardsEndpoint.replace(":customer_id",App.appContextHolder.getCustomerId());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":reward_id", rewardId);
        String jsonResponse = apiService.call(url, params, "post");

        this.webEngine.executeScript("redeemRewardsResponseHandler('"+jsonResponse+"')");
    }

}
