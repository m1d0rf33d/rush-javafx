package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Token;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.*;

/** All API calls that will be made going to Rush API should be here / API Module
 *
 *  @author m1d0rf33d
 */
public class ApiService {



    public JSONObject call(String url, List<NameValuePair> params, String method, String resourceOwner) {

        try {
            String token = getToken(resourceOwner);

            HttpResponse response = null;
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            //POST request
            if (method.equalsIgnoreCase("post")) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpPost.addHeader("Authorization", "Bearer "+ token);
                httpPost.addHeader("X-App", "POS-Sync");
                response = httpClient.execute(httpPost);
            }
            //GET request
            if (method.equalsIgnoreCase("get")) {
                HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                request.addHeader("Authorization", "Bearer "+ token);
                request.addHeader("X-App", "POS-Sync");
                response = httpClient.execute(request);
            }
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(result.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getToken(String resourceOwner) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String appKey, appSecret;
        if (resourceOwner.equals(ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER)) {
            appKey = MERCHANT_APP_KEY;
            appSecret = MERCHANT_APP_SECRET;
        } else {
            appKey = CUSTOMER_APP_KEY;
            appSecret = CUSTOMER_APP_SECRET;
        }

        String url = BASE_URL + AUTHORIZATION_ENDPOINT;
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app_key", appKey));
        params.add(new BasicNameValuePair("app_secret", appSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = httpClient.execute(httpPost);
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        Token token = mapper.readValue(result.toString(), Token.class);
        return token.getToken();
    }

    public JSONObject getOauth2Token() {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            String url = CMS_URL + TOMCAT_PORT + OAUTH_ENDPOINT;
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", OAUTH_SECRET);
            httpPost.addHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(httpPost);
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(result.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject callWidgetAPI(String url, String type) {

        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = null;
            if (type.equalsIgnoreCase("get")) {
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader("Authorization", "Bearer " + getOauth2Token());
                httpGet.addHeader("Content-Type", "application/json");
                response = httpClient.execute(httpGet);
            }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(sb.toString());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
