package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Token;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aomine on 9/19/16.
 */
public class ApiService {

    public String call(String url, List<NameValuePair> params, String method) {
        //Validate token
        if (App.appContextHolder.getAuthorizationToken() == null) {
            App.appContextHolder.setAuthorizationToken(getToken());
        }

        HttpResponse response = null;
        HttpClient client = HttpClientBuilder.create().build();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //POST request
            if (method.equalsIgnoreCase("post")) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpPost.addHeader("Authorization", "Bearer "+ App.appContextHolder.getAuthorizationToken());
                response = client.execute(httpPost);
            }
            //GET request
            if (method.equalsIgnoreCase("get")) {
                HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                request.addHeader("Authorization", "Bearer "+ App.appContextHolder.getAuthorizationToken());
                response = client.execute(request);
            }
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private String getToken() {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://52.74.203.202/api/dev/token");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("app_key", "IBVAkQ8GmVpJ"));
            params.add(new BasicNameValuePair("app_secret", "QbijavHPlAt4mNgGpaIsY0ju9fQ11igN"));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = client.execute(httpPost);
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
