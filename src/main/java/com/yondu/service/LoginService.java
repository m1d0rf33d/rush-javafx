package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.model.Account;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.scene.web.WebEngine;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

/** Service for Login Module
 *
 */
public class LoginService {
    //TODO: Try to implement dependency injection via com.airhackes.igniter
    private ApiService apiService = new ApiService();
    private WebEngine webEngine;

    public LoginService(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public void login() {
        ApiResponse<Account> apiResponse = new ApiResponse();
        HTMLInputElement employeeField = (HTMLInputElement) this.webEngine.getDocument().getElementById("employeeId");
        HTMLSelectElement selectField = (HTMLSelectElement) this.webEngine.getDocument().getElementById("branchId");
        String empId = employeeField.getValue();
        String branchId = selectField.getValue();
        String url = "http://52.74.203.202/api/dev/loyalty/merchantapp/employee/login";
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_id", employeeField.getValue()));
        params.add(new BasicNameValuePair("branch_id", selectField.getValue()));
        String result = apiService.call(url, params, "post");

        if (result.contains("0x1")) {
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
        webEngine.executeScript("loginSuccess()");
    }


    public void loadBranches(final Object callbackfunction) {

        ApiResponse apiResponse = new ApiResponse();

        String url = "http://52.74.203.202/api/dev/loyalty/merchantapp/merchant/branches";
        List<NameValuePair> params = new ArrayList<>();
        String result = apiService.call(url, params, "get");

        ObjectMapper mapper = new ObjectMapper();
        JSONParser parser = new JSONParser();
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
