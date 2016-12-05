package com.yondu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.yondu.App;
import com.yondu.AppContextHolder;
import com.yondu.Browser;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_FXML;
import static com.yondu.model.constants.AppConfigConstants.SETTINGS_FXML;
import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;

/** Home Module services / Java2Javascript bridge
 *  Methods inside this class can be invoked inside a javascript using alert("__CONNECT__BACKEND__homeService")
 *
 *  @author m1d0rf33d
 */
public class HomeService {

    private WebEngine webEngine;
    private ApiService apiService = new ApiService();

    private Stage ocrConfigStage;
    private Stage givePointsStage;
    private WebView webView;

    public HomeService(WebEngine webEngine,
                       WebView webView) {
        this.webView = webView;
        this.webEngine = webEngine;
    }

    /** Load employee data that will be sent back to the calling javascript. Target page view-> home.html
     *
     * @param callbackfunction
     */
    public void loadEmployeeData(final Object callbackfunction) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-YYYY");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", App.appContextHolder.getEmployeeId());
            jsonObject.put("name",App.appContextHolder.getEmployeeName());
            jsonObject.put("currentDate",formatter.format(new Date()));
            //Load branches
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetBranchesEndpoint();
            List<NameValuePair> params = new ArrayList<>();
            String jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            List<JSONObject> data = (ArrayList) jsonObj.get("data");
            for (JSONObject branch : data) {
                if (branch.get("id").equals(App.appContextHolder.getBranchId())) {
                    jsonObject.put("branchName", branch.get("name"));
                    jsonObject.put("branchLogo", branch.get("logo_url"));
                    break;
                }
            }

            url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMerchantDesignsEndpoint();
            params = new ArrayList<>();
            jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            jsonObj = (JSONObject) parser.parse(jsonResponse);
            JSONObject d = (JSONObject) jsonObj.get("data");
            JSONObject merchant = (JSONObject) d.get("merchant");
            jsonObject.put("backgroundUrl", merchant.get("background_url"));


            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost("http://52.74.203.202:8080/rush-pos-sync/oauth/token?grant_type=password&username=admin&password=admin&client_id=clientIdPassword");
            httpPost.addHeader("Authorization", "Basic Y2xpZW50SWRQYXNzd29yZDpzZWNyZXQ=");
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
            JSONObject jsonObj1 = (JSONObject) parser.parse(result.toString());
            String token = (String) jsonObj1.get("access_token");
            HttpGet httpGet = new HttpGet("http://52.74.203.202:8080/rush-pos-sync/api/merchant/access/" + App.appContextHolder.getEmployeeId() +"/" + App.appContextHolder.getBranchId());
            httpGet.addHeader("Authorization", "Bearer " + token);
            httpGet.addHeader("Content-Type", "application/json");
            response = httpClient.execute(httpGet);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

             result = new StringBuffer();
             line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JSONObject j = (JSONObject) parser.parse(result.toString());
            JSONObject dataJson = (JSONObject) j.get("data");
            List<String> screens = (ArrayList) dataJson.get("access");
            Boolean withVk = (Boolean) dataJson.get("withVk");
            App.appContextHolder.setWithVk(withVk);
            jsonObject.put("screens", screens);

            httpClient.close();
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, jsonObject.toJSONString());
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            redirectToSplash();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /** Register new member, a javascript response handler function will be called to handle the result.
     *
     */
    public void register(String name, String email, String mobile, String mpin, String birthdate, String gender, Object callbackfunction) {
        String jsonResponse = null;
        try {
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, name));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, email));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobile));
            params.add(new BasicNameValuePair(ApiFieldContants.PIN, mpin));
            //Optional fields
            if (birthdate != null && !birthdate.isEmpty()) {

                params.add(new BasicNameValuePair(ApiFieldContants.BIRTHDATE, birthdate));
            }
            if (gender != null && !gender.isEmpty()) {
                params.add(new BasicNameValuePair(ApiFieldContants.GENDER, gender));
            }

            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getRegisterEndpoint().replace(":employee_id", App.appContextHolder.getEmployeeId());
            jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            final String d = jsonResponse;
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, d)
            ).start();

            this.webEngine.executeScript("registerResponseHandler('"+d+"')");
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
        }
        webEngine.executeScript("closeLoadingModal('"+ App.appContextHolder.isOnlineMode()+"')");
    }

    /** Login member
     *
     * @param callbackfunction
     */
    public void loginMember(String mobileNumber, final Object callbackfunction) {
        try {
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNumber));

            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String result = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result);
            String error = (String) jsonObject.get(ApiFieldContants.ERROR_CODE);
            if (error.equals(ApiFieldContants.NO_ERROR)) {
                JSONObject data = (JSONObject) jsonObject.get(ApiFieldContants.DATA);
                App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                App.appContextHolder.setCustomerUUID((String) data.get("id"));

                //get current points
                params = new ArrayList<>();
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint();
                url = url.replace(":customer_uuid", App.appContextHolder.getCustomerUUID());
                String jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONObject json = (JSONObject) parser.parse(jsonResponse);
                data.put("points", json.get("data"));

                //get member rewards
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getCustomerRewardsEndpoint();
                url = url.replace(":id", App.appContextHolder.getCustomerUUID());
                String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
                //Parse results
                JSONObject j = (JSONObject) parser.parse(responseStr);
                List<JSONObject> d = (ArrayList) j.get("data");
                responseStr = new Gson().toJson(d);
                data.put("activeVouchers", responseStr);
                result = jsonObject.toJSONString();
            }
            String finalData = result;
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, finalData)
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
            e.printStackTrace();

        } catch (ParseException e) {
            //invalid response format
            e.printStackTrace();
        }
        webEngine.executeScript("closeLoadingModal('" + App.appContextHolder.isOnlineMode() + "')");

    }

    /** Pay using points
     *
     */
    public void payWithPoints(String points, String orNumber, String amount) {
       try {
           DecimalFormat formatter = new DecimalFormat("#,###.00");
           List<NameValuePair> params = new ArrayList<>();
           params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
           params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
           params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
           params.add(new BasicNameValuePair(ApiFieldContants.POINTS, points.replace(",","")));

           String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getPayWithPointsEndpoint();
           url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
           String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
           jsonResponse = jsonResponse.replace("'","");

           JSONParser parser = new JSONParser();
           JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
           String error =  (String) jsonObj.get(ApiFieldContants.ERROR_CODE);
           if (error.equals(ApiFieldContants.NO_ERROR)) {
               //get current points
               params = new ArrayList<>();
               url =  App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint();
               url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
               String result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
               //Parse response
               JSONObject resultJson = (JSONObject) parser.parse(result);
               jsonObj.put("points", formatter.format(resultJson.get("data")));
               if(jsonObj.get("points").equals(".00")) {
                   jsonObj.put("points", "0");
               }
               //Convert to peso value
               url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getPointsConversionEndpoint();
               url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
               result = App.appContextHolder.getApiService().call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
               JSONObject obj1 = (JSONObject) parser.parse(result);
               JSONObject d1 = (JSONObject) obj1.get("data");


               Double dPoints = null;
               Double redemptionPeso = null;
               Long redemptionPoints = (Long) d1.get("redemption_points");
               try {
                   dPoints = (Double) resultJson.get("data");
               } catch (ClassCastException e) {
                   Long po = (Long) resultJson.get("data");
                   dPoints = po.doubleValue();
               }
               dPoints = dPoints / redemptionPoints;
               try {
                   redemptionPeso = (Double) d1.get("redemption_peso");
               } catch (ClassCastException e) {
                   Long ex = (Long) d1.get("redemption_peso");
                   redemptionPeso = ex.doubleValue();
               }
               jsonObj.put("pointsPesoValue", formatter.format( dPoints * redemptionPeso));
               if (jsonObj.get("pointsPesoValue").equals(".00")) {
                   jsonObj.put("pointsPesoValue","0");
               }
               jsonResponse = jsonObj.toJSONString();
           }

           webEngine.executeScript("payWithPointsResponse('"+jsonResponse+"')");
       } catch (IOException e) {
           App.appContextHolder.setOnlineMode(false);
           goToOfflineMode();
           e.printStackTrace();
       } catch (ParseException e) {
           e.printStackTrace();
       }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    /** Load all merchant rewards
     *
     * @param callbackfunction
     */
    public void loadRewards(final Object callbackfunction) {

        try {
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetRewardsMerchantEndpoint();
            String jsonResponse = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            final String data = jsonResponse;
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, data)
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    /** Redeeem member reward
     *
     * @param rewardId
     * @param pin
     */
    public void redeemRewards(String rewardId, String pin) {

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getRedeemRewardsEndpoint();
            url = url.replace(":customer_id",App.appContextHolder.getCustomerUUID());
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            url = url.replace(":reward_id", rewardId);
            String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            responseStr  = responseStr.replace("'","");
            //retrieve current points
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
            //get current points
            params = new ArrayList<>();
            url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint();
            url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            String res = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONObject json = (JSONObject) parser.parse(res);
            jsonResponse.put("points",  json.get("data"));
            responseStr = jsonResponse.toJSONString();

            this.webEngine.executeScript("redeemRewardsResponseHandler('"+responseStr+"')");
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void loadCustomerRewards(final Object callbackfunction) {
        try {
            String tempdata = "";
            //Retrieve all rewards
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetRewardsEndpoint();
            String jsonResponse = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject rewardsJson = (JSONObject) parser.parse(jsonResponse);
            List<JSONObject> rewardsDataList = (ArrayList) rewardsJson.get("data");

            url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getUnclaimedRewardsEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            url = url.replace(":customer_id", App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONObject unclaimedJson = (JSONObject) parser.parse(responseStr);
            List<JSONObject> unclaimedDataList = (ArrayList) unclaimedJson.get("data");
            for (JSONObject unclaimedData : unclaimedDataList) {
                JSONObject reward = (JSONObject) unclaimedData.get("reward");
                String rewardName = (String) reward.get("name");
                for (JSONObject rewardsData: rewardsDataList) {
                    String rName = (String) rewardsData.get("name");
                    if (rName.equals(rewardName)) {
                        reward.put("details", (String) rewardsData.get("details"));
                        reward.put("image_url", (String) rewardsData.get("image_url"));
                        break;
                    }
                }
            }
            tempdata = unclaimedJson.toJSONString();
            final String finalData = tempdata;
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, finalData)
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
            goToOfflineMode();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }


    public void issueReward(String redeemId) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.REDEEM_ID, redeemId));
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getClaimRewardsEndpoint();
            url = url.replace(":customer_id",App.appContextHolder.getCustomerUUID());
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            //Parse response
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            jsonObj.put("redeemId", redeemId);
            jsonResponse = jsonObj.toJSONString();
            this.webEngine.executeScript("issueRewardsResponseHandler('"+jsonResponse+"')");
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
            goToOfflineMode();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void loadSettingsView() {
        try {
            if (ocrConfigStage != null) {
                ocrConfigStage.close();
            }
            ocrConfigStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(SETTINGS_FXML));
            ocrConfigStage.setScene(new Scene(root, 700,500));
            ocrConfigStage.setTitle("Rush POS Sync");
            ocrConfigStage.resizableProperty().setValue(Boolean.FALSE);
            ocrConfigStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            ocrConfigStage.show();
            App.appContextHolder.getHomeStage().close();
        } catch (IOException e) {
            goToOfflineMode();
            e.printStackTrace();
        }
    }

    public void loadGivePointsView() {
        try {
            if (givePointsStage != null) {
                givePointsStage.close();
            }
            givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
            givePointsStage.setScene(new Scene(root, 400,220));
            givePointsStage.setTitle("Rush POS Sync");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);

            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();

            App.appContextHolder.getHomeStage().close();

        } catch (IOException e) {
            e.printStackTrace();
            goToOfflineMode();

        }
    }

    public void logoutEmployee() {
        //cleanr AppContextHolder
        App.appContextHolder.setEmployeeId(null);
        App.appContextHolder.setEmployeeName(null);
        App.appContextHolder.setCustomerMobile(null);
        //redirect to login page
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();
            Stage stage = new Stage();
            //Let's get the party started
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
            stage.setScene(new Scene(root, 600,400));
            stage.resizableProperty().setValue(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.show();
            App.appContextHolder.getHomeStage().close();
            App.appContextHolder.setHomeStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
            goToOfflineMode();
        }
    }

    public void fetchCustomerData(Object callbackfunction) {
        String result = "";
        if (App.appContextHolder.getCustomerMobile() != null && App.appContextHolder.getCustomerUUID() != null) {

            try {
                DecimalFormat formatter = new DecimalFormat("#,###,###.00");
                //Build request body
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, App.appContextHolder.getCustomerMobile()));

                String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
                url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
                result = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(result);
                JSONObject data = (JSONObject) jsonResponse.get("data");
                //get current points
                params = new ArrayList<>();
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint();
                url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                JSONObject json = (JSONObject) parser.parse(result);
                Double p = (Double) json.get("data");
                data.put("points",  formatter.format(p));
                if (data.get("points").equals(".00")) {
                    data.put("points",  "0");
                }

                //get member rewards
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getCustomerRewardsEndpoint();
                url = url.replace(":id",App.appContextHolder.getCustomerUUID());
                String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
                //Parse response
                JSONObject j = (JSONObject) parser.parse(responseStr);
                List<JSONObject> d = (ArrayList) j.get("data");
                responseStr = new Gson().toJson(d);
                data.put("activeVouchers", responseStr);
                //Convert to peso value
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getPointsConversionEndpoint();
                url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
                result = App.appContextHolder.getApiService().call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONObject obj1 = (JSONObject) parser.parse(result);
                JSONObject d1 = (JSONObject) obj1.get("data");


                Double dPoints = null;
                Double redemptionPeso = null;
                Long redemptionPoints = (Long) d1.get("redemption_points");
                try {
                    dPoints = (Double) json.get("data");
                } catch (ClassCastException e) {
                    Long po = (Long) json.get("data");
                    dPoints = po.doubleValue();
                }
                dPoints = dPoints / redemptionPoints;
                try {
                    redemptionPeso = (Double) d1.get("redemption_peso");
                } catch (ClassCastException e) {
                    Long ex = (Long) d1.get("redemption_peso");
                    redemptionPeso = ex.doubleValue();
                }
                data.put("pointsPesoValue", formatter.format( dPoints * redemptionPeso));
                if (data.get("pointsPesoValue").equals(".00")) {
                    data.put("pointsPesoValue","0");
                }

                result = jsonResponse.toJSONString();
                final String finalData = result;
                new Thread(()->
                        Java2JavascriptUtils.call(callbackfunction, finalData)
                ).start();

            } catch(IOException e) {
                App.appContextHolder.setOnlineMode(false);
                e.printStackTrace();
                goToOfflineMode();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void getCustomerRewards(Object callbackfunction) {
        try {
            List params = new ArrayList<>();
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getCustomerRewardsEndpoint();
            url = url.replace(":id",App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            //Parse response
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseStr);
            List<JSONObject> data = (ArrayList) jsonObject.get("data");
            responseStr = new Gson().toJson(data);

            final String dataStr = responseStr;
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, dataStr)
            ).start();

        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void logoutMember() {
        App.appContextHolder.setCustomerUUID(null);
        App.appContextHolder.setCustomerMobile(null);
    }

    public void getCustomerTransactions(Object callbackfunction) {

        try {
            List params = new ArrayList<>();
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getCustomerTransactionsEndpoint();
            url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(responseStr);
            List<JSONObject> arr = (ArrayList) jsonObj.get("data");
            for (JSONObject obj : arr) {
                if (obj.get("receipt_no") == null) {
                    obj.put("receipt_no", obj.get("reference_code"));
                }
            }
           /* JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(responseStr);
            List<JSONObject> data = (ArrayList) jsonObj.get("data");
            for (JSONObject j : data) {
                if ((Long)j.get("type") == 1) {
                    j.put("typeStr", "Earn");
                } else if ((Long)j.get("type") == 2) {
                    j.put("typeStr", "Paypoints");
                }
                else if ((Long)j.get("type") == 3) {
                    j.put("typeStr", "Redeem");
                }
                else if ((Long)j.get("type") == 4) {
                    j.put("typeStr", "Void");
                }
                else if ((Long)j.get("type") == 5) {
                    j.put("typeStr", "Void-Paypoints");
                }
                else if ((Long)j.get("type") == 6) {
                    j.put("typeStr", "Void-Redeem");
                }
            }
            final String finalData = jsonObj.toJSONString();*/
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, jsonObj.toJSONString())
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
            goToOfflineMode();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void getOfflineTransactions(Object callbackFunction) {
        File file = new File(App.appContextHolder.getOfflinePath());
        if (file.exists()) {
            JSONArray jsonArray = new JSONArray();
            //Read file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("mobileNumber", arr[0].split("=")[1]);
                    jsonObject.put("totalAmount", arr[1].split("=")[1]);
                    jsonObject.put("orNumber", arr[2].split("=")[1]);
                    jsonObject.put("date", arr[3].split("=")[1]);
                    jsonArray.add(jsonObject);
                }
            } catch (Exception e) {
                App.appContextHolder.setOnlineMode(false);
                e.printStackTrace();
            }

            final String finalData = jsonArray.toJSONString();
            new Thread(()->
                    Java2JavascriptUtils.call(callbackFunction, finalData)
            ).start();

        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void sendOfflinePoints() {
        File file = new File(App.appContextHolder.getOfflinePath());

        if (file.exists()) {
            JSONArray failedArray = new JSONArray();
            JSONArray successArray = new JSONArray();
            //Read file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    String mobileNumber = arr[0].split("=")[1];
                    String totalAmount = arr[1].split("=")[1];
                    String orNumber = arr[2].split("=")[1];
                    String date = arr[3].split("=")[1];

                    JSONObject json = new JSONObject();
                    json.put("mobileNumber",mobileNumber);
                    json.put("totalAmount", totalAmount);
                    json.put("orNumber", orNumber);
                    json.put("date", date);
                    //Retrieve customer information
                    //Build request body
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNumber));

                    String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
                    url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
                    String resultJson = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(resultJson);
                    if (!jsonObject.get("error_code").equals("0x0")) {

                        json.put("message", "No member found with corresponding mobile number.");
                        failedArray.add(json);
                    } else {
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        params = new ArrayList<>();
                        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
                        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmount.replace(",","")));
                        url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGivePointsEndpoint();
                        url = url.replace(":customer_uuid", (String) data.get("id"));
                        resultJson = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                        jsonObject = (JSONObject) parser.parse(resultJson);
                        if (!jsonObject.get("error_code").equals("0x0")) {
                            JSONObject error = (JSONObject) jsonObject.get("errors");
                            String errorMessage = "";
                           if (error != null) {
                               if (error.get("or_no") != null) {
                                   List<String> l = (ArrayList<String>) error.get("or_no");
                                   errorMessage = l.get(0);
                               }
                               if (error.get("amount") != null) {
                                   List<String> l = (ArrayList<String>) error.get("amount");
                                   errorMessage = l.get(0);
                               }
                           }
                           if (jsonObject.get("message") != null) {
                               errorMessage = (String)jsonObject.get("message");
                           }
                            json.put("message", errorMessage);
                            failedArray.add(json);
                        }  else {
                            successArray.add(json);
                        }
                    }
                }
                //Clear offline.txt
                PrintWriter writer = new PrintWriter(file);
                writer.print("");
                writer.close();

                br.close();
                JSONObject finalJson = new JSONObject();
                finalJson.put("successArray", successArray);
                finalJson.put("failedArray", failedArray);
                this.webEngine.executeScript("sendOfflinePointsResponse('"+ finalJson.toJSONString().replace("'","")+"')");
            } catch (IOException e) {
                App.appContextHolder.setOnlineMode(false);
                goToOfflineMode();
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
        }
    }

    public void givePointsManual(String orNumber, String amount) {

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
            params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
            params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGivePointsEndpoint();
            url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseStr);
            if (jsonObject.get("error_code").equals("0x0")) {
                //get current points
                params = new ArrayList<>();
                url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint();
                url = url.replace(":customer_uuid", App.appContextHolder.getCustomerUUID());
                String jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONObject json = (JSONObject) parser.parse(jsonResponse);
                jsonObject.put("points", json.get("data"));
                responseStr = jsonObject.toJSONString();
            } else {
                responseStr = jsonObject.toJSONString();

            }
            responseStr = responseStr.replace("'","");
            webEngine.executeScript("givePointsManualResponse('"+responseStr+"')");
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void getPointsRule(Object callbackFunction) {


        try {
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getPointsConversionEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
            String result = App.appContextHolder.getApiService().call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            new Thread(()->
                    Java2JavascriptUtils.call(callbackFunction, result)
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
            goToOfflineMode();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }
    private void redirectToSplash() {
        App.appContextHolder.setEmployeeId(null);
        App.appContextHolder.setEmployeeName(null);
        App.appContextHolder.setCustomerMobile(null);
        try {
            App.appContextHolder.getHomeStage().close();
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
            primaryStage.setScene(new Scene(root, 600,400));
            primaryStage.resizableProperty().setValue(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showVirtualKeyboard() {
       if(App.appContextHolder.getWithVk()) {
           FXVK.init(webView);
           FXVK.attach(webView);
       }
    }
    public void hideVirtualKeyboard() {

        if (App.appContextHolder.getWithVk()) {
            FXVK.detach();
        }
    }


    public void givePointsGuest(String mobileNo, String orNumber, String amount) {

        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNo));
            params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
            params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGuestEarnEndpoint();
            url = url.replace(":employee_id",App.appContextHolder.getEmployeeId());
            String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            responseStr = responseStr.replace("'","");
            webEngine.executeScript("givePointsGuestResponse('"+responseStr+"')");
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            goToOfflineMode();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void goToOfflineMode() {
        //Logout employee
        App.appContextHolder.setEmployeeId(null);
        App.appContextHolder.setEmployeeName(null);
        App.appContextHolder.setCustomerMobile(null);
        App.appContextHolder.setOnlineMode(false);


        try {
            App.appContextHolder.getHomeStage().close();


            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/give-points-manual.fxml"));
            givePointsStage.setScene(new Scene(root, 500,300));
            givePointsStage.setTitle("Rush POS Sync");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have been redirected to offline mode due to network connection failure. Check your internet connection and press home button to reconnect.", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(givePointsStage);
            alert.show();

            if (alert.getResult() == ButtonType.OK) {
                alert.close();
            }
            App.appContextHolder.setHomeStage(givePointsStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean checkConnectivity() {
        try {
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetBranchesEndpoint();
            java.util.List<NameValuePair> params = new ArrayList<>();
            String jsonResponse = App.appContextHolder.getApiService().call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public void goToOfflineOcrMode() {
        //Logout employee
        App.appContextHolder.setEmployeeId(null);
        App.appContextHolder.setEmployeeName(null);
        App.appContextHolder.setCustomerMobile(null);
        App.appContextHolder.setOnlineMode(false);


        try {
            App.appContextHolder.getHomeStage().close();

            if (givePointsStage != null) {
                givePointsStage.close();
            }
            givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
            givePointsStage.setScene(new Scene(root, 400,220));
            givePointsStage.setTitle("Rush POS Sync");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);

            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "You have been redirected to offline mode due to network connection failure. Check your internet connection and press home button to reconnect.", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(givePointsStage);
            alert.show();

            if (alert.getResult() == ButtonType.OK) {
                alert.close();
            }
            App.appContextHolder.setHomeStage(givePointsStage);

        } catch (IOException e) {
            e.printStackTrace();
            goToOfflineMode();

        }
    }
}
