package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.model.Token;
import com.yondu.model.constants.ApiFieldContants;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
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
            DefaultHttpClient client = new DefaultHttpClient();
            DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
            client.setHttpRequestRetryHandler(retryHandler);

            //POST request
            if (method.equalsIgnoreCase("post")) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpPost.addHeader("Authorization", "Bearer "+ token);
                httpPost.addHeader("X-App", "POS-Sync");
                response = client.execute(httpPost);
            }
            //GET request
            if (method.equalsIgnoreCase("get")) {
                HttpGet request = new HttpGet(url);
                request.addHeader("content-type", "application/json");
                request.addHeader("Authorization", "Bearer "+ token);
                request.addHeader("X-App", "POS-Sync");
                response = client.execute(request);
            }
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            client.close();
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
        DefaultHttpClient httpClient = new DefaultHttpClient();
        DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
        httpClient.setHttpRequestRetryHandler(retryHandler);
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
        List<NameValuePair> params = new ArrayList<>();
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
        httpClient.close();

        ObjectMapper mapper = new ObjectMapper();
        Token token = mapper.readValue(result.toString(), Token.class);
        return token.getToken();
    }

    public JSONObject getOauth2Token() {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
            client.setHttpRequestRetryHandler(retryHandler);

            String url = CMS_URL + TOMCAT_PORT + OAUTH_ENDPOINT;
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", OAUTH_SECRET);
            httpPost.addHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(httpPost);
            // use httpClient (no need to close it explicitly)
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            client.close();
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

    public JSONObject callWidgetAPI(String url, JSONObject jsonObject, String type) {

        try {
            JSONObject tokenJSON = getOauth2Token();
            if (tokenJSON == null) {
                throw new IOException();
            }

            StringEntity stringEntity = new StringEntity(jsonObject.toJSONString());
            String token = (String) tokenJSON.get("access_token");
            DefaultHttpClient client = new DefaultHttpClient();
            DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
            client.setHttpRequestRetryHandler(retryHandler);

            HttpResponse response;
            if (type.equalsIgnoreCase("get")) {
                HttpGet httpGet = new HttpGet(url);
                httpGet.addHeader("Authorization", "Bearer " + token);
                httpGet.addHeader("Content-Type", "application/json");
                response = client.execute(httpGet);
            } else {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(stringEntity);
                httpPost.addHeader("Authorization", "Bearer " + token);
                httpPost.addHeader("Content-Type", "application/json");
                response = client.execute(httpPost);
            }

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            client.close();
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
