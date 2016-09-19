package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Token;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.fxml.Initializable;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

/**
 * Created by aomine on 9/19/16.
 */
public class LoginService {

    private ApiService apiService = new ApiService();

    // async function
    public void loadBranches(final Object callbackfunction) {

        ApiResponse apiResponse = new ApiResponse();

        String url = "http://52.74.203.202/api/dev/loyalty/merchantapp/merchant/branches";
        List<NameValuePair> params = new ArrayList<>();
        String result = apiService.call(url, params, "get");

        ObjectMapper mapper = new ObjectMapper();
        try {
            apiResponse = mapper.readValue(result, ApiResponse.class);

        } catch (IOException e) {
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
