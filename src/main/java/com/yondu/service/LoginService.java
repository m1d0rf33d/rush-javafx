package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.ApiFieldContants;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.enums.ApiError;
import com.yondu.utils.Java2JavascriptUtils;
import com.yondu.utils.ResourcePropertyUtil;
import javafx.scene.web.WebEngine;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

/** Service for Login Module / Java2Javascript Bridge
 *  Methods inside this class can be invoked in javascript using alert("__CONNECT__BACKEND__loginService")
 *
 *  @author m1d0rf33d
 */
public class LoginService {

    private ApiService apiService = new ApiService();
    private WebEngine webEngine;

    private String baseUrl;
    private String loginApi;

    public LoginService(WebEngine webEngine) {
        this.webEngine = webEngine;
        //Load property values
        this.baseUrl = ResourcePropertyUtil.getProperty("api.properties", "base_url", this.getClass());
        this.loginApi = ResourcePropertyUtil.getProperty("api.properties", "login_api", this.getClass());
    }

    public void login() {
        ApiResponse apiResponse = new ApiResponse();
        //Read html form values
        HTMLInputElement employeeField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.EMPLOYEE_ID);
        HTMLSelectElement selectField = (HTMLSelectElement) this.webEngine.getDocument().getElementById(ApiFieldContants.BRANCH_ID);
        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, employeeField.getValue()));
        params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, selectField.getValue()));
        String result = apiService.call((baseUrl + loginApi), params, "post");

        //Validate errors
        if (result.contains(String.valueOf(ApiError.x10))) {
            //Call javascript function that will notify user
            webEngine.executeScript("loginFailed()");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        Account account;
        try {
            apiResponse = mapper.readValue(result, ApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Set logged in employee in application context
        LinkedHashMap map = (LinkedHashMap) apiResponse.getData();
        App.appContextHolder.setEmployeeName(((String) map.get("name")));
        webEngine.executeScript("loginSuccess()");
    }

    public void loadBranches(final Object callbackfunction) {
        ApiService apiService = new ApiService();
        ApiResponse apiResponse = new ApiResponse();

        String url = "http://52.74.203.202/api/dev/loyalty/merchantapp/merchant/branches";
        List<NameValuePair> params = new ArrayList<>();
        String result = apiService.call(url, params, "get");

        ObjectMapper mapper = new ObjectMapper();
        try {
            apiResponse = mapper.readValue(result, ApiResponse.class);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        final List<Branch> data = (List<Branch>) apiResponse.getData();
        // launch a background thread (async)
        new Thread( () -> {
            try {
                sleep(1000); //add some processing simulation...
                runLater( () ->
                        Java2JavascriptUtils.call(callbackfunction, toJSONString(data))
                );
            } catch (InterruptedException e) {	}
        }
        ).start();
    }

}
