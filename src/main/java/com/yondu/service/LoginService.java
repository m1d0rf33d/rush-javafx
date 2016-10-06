package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.scene.web.WebEngine;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.parse;
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
    private String loginEndpoint;
    private String getBranchesEndpoint;

    public LoginService(WebEngine webEngine) {
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
            this.loginEndpoint = prop.getProperty("login_endpoint");
           // this.getBranchesEndpoint = prop.getProperty("get_branches_endpoint");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        String jsonResponse = apiService.call((baseUrl + loginEndpoint), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parse(jsonResponse);
        if (jsonObject.get("data") != null) {
            JSONObject data = (JSONObject) jsonObject.get("data");
            App.appContextHolder.setEmployeeName(((String) data.get("name")));
            App.appContextHolder.setEmployeeId((String) data.get("id"));
        }

        webEngine.executeScript("loginResponseHandler('"+jsonResponse+"')");
    }

    public void loadBranches(final Object callbackfunction) {
        ApiService apiService = new ApiService();
        ApiResponse apiResponse = new ApiResponse();

        String url = "http://52.74.203.202/api/dev/loyalty/merchantapp/merchant/branches";
        List<NameValuePair> params = new ArrayList<>();
        String result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        ObjectMapper mapper = new ObjectMapper();
        try {
            apiResponse = mapper.readValue(result, ApiResponse.class);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        final List<Branch> data = (List<Branch>) apiResponse.getData();
        webEngine.executeScript("closeLoadingModal()");
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
