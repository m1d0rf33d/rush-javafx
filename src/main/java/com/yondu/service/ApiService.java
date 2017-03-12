package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.model.Token;
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
import static com.yondu.model.constants.ApiConstants.*;

/** All API calls that will be made going to Rush API should be here / API Module
 *
 *  @author m1d0rf33d
 */
public class ApiService  {


    public JSONObject callWidget(String url, String jsonString, String method, String token) {

        try {

            StringEntity stringEntity = null;
            if (jsonString != null) {
                stringEntity  = new StringEntity(jsonString);
            }

            DefaultHttpClient client = new DefaultHttpClient();
            DefaultHttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(5, true);
            client.setHttpRequestRetryHandler(retryHandler);

            HttpResponse response;
            if (method.equalsIgnoreCase("get")) {
                HttpGet httpGet = new HttpGet(url);
                if (token != null) {
                    httpGet.addHeader("Authorization", "Bearer " + token);
                }
                httpGet.addHeader("Content-Type", "application/json");
                response = client.execute(httpGet);
            } else {
                HttpPost httpPost = new HttpPost(url);
                if (stringEntity != null) {
                    httpPost.setEntity(stringEntity);
                }
                if (token != null) {
                    httpPost.addHeader("Authorization", "Bearer " + token);
                }

                httpPost.addHeader("Content-Type", "application/json");
                response = client.execute(httpPost);
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

}
