package com.yondu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.ApiFieldContants;
import com.yondu.model.ApiResponse;
import com.yondu.model.enums.ApiError;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.scene.web.WebEngine;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEmployeeData(final Object callbackfunction) {

        Account account = new Account();
        account.setId("");
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

    public void register(final Object callbackfunction) {
        //Read html form values
        HTMLInputElement nameField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_NAME);
        HTMLInputElement emailField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_EMAIL);
        HTMLInputElement mobileField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_MOBILE);
        HTMLInputElement pinField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_PIN);

        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, nameField.getValue()));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, emailField.getValue()));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getValue()));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_PIN, pinField.getValue()));

        String url = baseUrl + registerEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());

        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, apiService.call(url, params, "post"));
        }
        ).start();
    }

    public void memberLogin(final Object callbackfunction) {
        //Read html form values
        HTMLInputElement mobileField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_MOBILE);

        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getValue()));

        String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
        String result = apiService.call(url, params, "post");

        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, result);
        }
        ).start();
    }
}
