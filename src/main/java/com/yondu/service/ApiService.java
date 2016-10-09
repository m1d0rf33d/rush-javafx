package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.AppContextHolder;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.Token;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** All API calls that will be made going to Rush API should be here / API Module
 *
 *  @author m1d0rf33d
 */
public class ApiService {

    public String call(String url, List<NameValuePair> params, String method, String resourceOwner) throws IOException {
        //Validate token
        String token = "";
        if (resourceOwner.equals(ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER)) {
            if (App.appContextHolder.getAuthorizationToken() == null) {
                App.appContextHolder.setAuthorizationToken(getToken(resourceOwner));
            }
            token = App.appContextHolder.getAuthorizationToken();
        } else {
            if (App.appContextHolder.getCustomerAppAuthToken() == null) {
                App.appContextHolder.setCustomerAppAuthToken(getToken(resourceOwner));
            }
            token = App.appContextHolder.getCustomerAppAuthToken();
        }


        HttpResponse response = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            //POST request
            if (method.equalsIgnoreCase("post")) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpPost.addHeader("Authorization", "Bearer "+ token);
                response = httpClient.execute(httpPost);
            }
            //GET request
            if (method.equalsIgnoreCase("get")) {
                HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                request.addHeader("Authorization", "Bearer "+ token);
                response = httpClient.execute(request);
            }
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            //Set application state to online
            App.appContextHolder.setOnlineMode(true);
            return result.toString();
    }


    public String getToken(String resourceOwner) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String appKey = "", appSecret = "";
        if (resourceOwner.equals(ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER)) {
            appKey = App.appContextHolder.getAppKey();
            appSecret = App.appContextHolder.getAppSecret();
        } else {
            appKey = App.appContextHolder.getCustomerAppKey();
            appSecret = App.appContextHolder.getCustomerAppSecret();
        }

        String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getAuthorizationEndpoint();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app_key", appKey));
        params.add(new BasicNameValuePair("app_secret", appSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = httpClient.execute(httpPost);
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        Token token = mapper.readValue(result.toString(), Token.class);
        return token.getToken();
    }
}
