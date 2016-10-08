package com.yondu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_FXML;
import static com.yondu.model.constants.AppConfigConstants.SETTINGS_FXML;

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
    private String pointsConversionEndpoint;
    private String givePointsEndpoint;
    private String getPointsEndpoint;
    private String payWithPointsEndpoint;
    private String getRewardsEndpoint;
    private String redeemRewardsEndpoint;
    private String unclaimedRewardsEndpoint;
    private String claimRewardsEndpoint;
    private String getRewardsMerchantEndpoint;
    private String customerRewardsEndpoint;
    private String customerTransactionsEndpoint;

    private Stage ocrConfigStage;
    private Stage givePointsStage;

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
            this.pointsConversionEndpoint = prop.getProperty("points_conversion_endpoint");
            this.givePointsEndpoint = prop.getProperty("give_points_endpoint");
            this.getPointsEndpoint = prop.getProperty("get_points_endpoint");
            this.payWithPointsEndpoint = prop.getProperty("pay_points_endpoint");
            this.getRewardsEndpoint = prop.getProperty("get_rewards_endpoint");
            this.redeemRewardsEndpoint = prop.getProperty("redeem_rewards_endpoint");
            this.unclaimedRewardsEndpoint = prop.getProperty("unclaimed_rewards_endpoint");
            this.claimRewardsEndpoint = prop.getProperty("claim_rewards_endpoint");
            this.getRewardsMerchantEndpoint =prop.getProperty("get_rewards_merchant_endpoint");
            this.customerRewardsEndpoint = prop.getProperty("customer_rewards_endpoint");
            this.customerTransactionsEndpoint = prop.getProperty("customer_transactions_endpoint");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Load employee data that will be sent back to the calling javascript. Target page view-> home.html
     *
     * @param callbackfunction
     */
    public void loadEmployeeData(final Object callbackfunction) {
        Account account = new Account();
        account.setId(App.appContextHolder.getEmployeeId());
        account.setName(App.appContextHolder.getEmployeeName());

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-YYYY");
        account.setCurrentDate(formatter.format(new Date()));

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

    /** Register new member, a javascript response handler function will be called to handle the result.
     *
     */
    public void register() {
        try {
            //Read html form values
            HTMLInputElement nameField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_NAME);
            HTMLInputElement emailField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_EMAIL);
            HTMLInputElement mobileField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MEMBER_MOBILE);
            HTMLInputElement mpinField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.MPIN);
            HTMLInputElement birthdateField = (HTMLInputElement) this.webEngine.getDocument().getElementById(ApiFieldContants.BIRTHDATE);
            HTMLSelectElement genderField = (HTMLSelectElement) this.webEngine.getDocument().getElementById(ApiFieldContants.GENDER);

            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, nameField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, emailField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getValue()));
            params.add(new BasicNameValuePair(ApiFieldContants.MPIN, mpinField.getValue()));
            //Optional fields
            if (birthdateField.getValue() != null && !birthdateField.getValue().isEmpty()) {
                params.add(new BasicNameValuePair(ApiFieldContants.BIRTHDATE, birthdateField.getValue()));
            }
            if (genderField.getValue() != null && !genderField.getValue().isEmpty()) {
                params.add(new BasicNameValuePair(ApiFieldContants.GENDER, genderField.getValue()));
            }


            String url = baseUrl + registerEndpoint;
            String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            this.webEngine.executeScript("registerResponseHandler('"+jsonResponse+"')");
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
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

            String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String result = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);


            JSONParser parser = new JSONParser();

            JSONObject jsonObject = (JSONObject) parser.parse(result);
            String error =  (String) jsonObject.get(ApiFieldContants.ERROR_CODE);
            if (error.equals(ApiFieldContants.NO_ERROR)) {
                JSONObject data =  (JSONObject) jsonObject.get(ApiFieldContants.DATA);
                App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                App.appContextHolder.setCustomerUUID((String) data.get("id"));

                //get current points
                params = new ArrayList<>();
                url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                String jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONObject json = (JSONObject) parser.parse(jsonResponse);
                data.put("points",  json.get("data"));

                //get member rewards
                url = baseUrl + customerRewardsEndpoint.replace(":id",App.appContextHolder.getCustomerUUID());
                String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
                JSONObject j = (JSONObject) parser.parse(responseStr);
                List<JSONObject> d = (ArrayList) j.get("data");
                responseStr = new Gson().toJson(d);
                data.put("activeVouchers", responseStr);
                result = jsonObject.toJSONString();
            }

            final String responseStr = result;
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, responseStr);
            }).start();
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
            //offline mode
        } catch (ParseException e) {
           //invalid response format
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    /** Pay using points
     *
     */
    public void payWithPoints(String points, String orNumber, String amount) {
       try {
           List<NameValuePair> params = new ArrayList<>();
           params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
           params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
           params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount));
           params.add(new BasicNameValuePair(ApiFieldContants.POINTS, points));
           String url = baseUrl + payWithPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
           String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
           jsonResponse = jsonResponse.replace("'","");

           JSONParser parser = new JSONParser();
           JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
           String error =  (String) jsonObj.get(ApiFieldContants.ERROR_CODE);
           if (error.equals(ApiFieldContants.NO_ERROR)) {
               //get current points
               params = new ArrayList<>();
               url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
               String result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
               JSONObject resultJson = (JSONObject) parser.parse(result);
               jsonObj.put("points", resultJson.get("data"));
               jsonResponse = jsonObj.toJSONString();
           }

           webEngine.executeScript("payWithPointsResponse('"+jsonResponse+"')");
       } catch (IOException e) {
           App.appContextHolder.setOnlineMode(false);
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
            String url = baseUrl + getRewardsMerchantEndpoint;
            String jsonResponse = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            final String data = jsonResponse;
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, data);
            }).start();
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
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
            String url = baseUrl + redeemRewardsEndpoint.replace(":customer_id",App.appContextHolder.getCustomerUUID());
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            url = url.replace(":reward_id", rewardId);
            String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            responseStr  = responseStr.replace("'","");
            //retrieve current points
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
            //get current points
            params = new ArrayList<>();
            url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            String res = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONObject json = (JSONObject) parser.parse(res);
            jsonResponse.put("points",  json.get("data"));
            responseStr = jsonResponse.toJSONString();

            this.webEngine.executeScript("redeemRewardsResponseHandler('"+responseStr+"')");
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
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
            String url = baseUrl + getRewardsEndpoint;
            String jsonResponse = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject rewardsJson = (JSONObject) parser.parse(jsonResponse);
            List<JSONObject> rewardsDataList = (ArrayList) rewardsJson.get("data");
            url = baseUrl + unclaimedRewardsEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
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
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, finalData);
            }).start();
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }


    public void issueReward(String redeemId) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.REDEEM_ID, redeemId));
            String url = baseUrl + claimRewardsEndpoint.replace(":customer_id",App.appContextHolder.getCustomerUUID());
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            jsonObj.put("redeemId", redeemId);
            jsonResponse = jsonObj.toJSONString();
            this.webEngine.executeScript("issueRewardsResponseHandler('"+jsonResponse+"')");
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
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
            ocrConfigStage.setTitle("Setup OCR");
            ocrConfigStage.getScene().getStylesheets().add(App.class.getResource("/app/css/fxml.css").toExternalForm());
            ocrConfigStage.resizableProperty().setValue(Boolean.FALSE);
            ocrConfigStage.show();
            App.appContextHolder.getHomeStage().close();
        } catch (IOException e) {
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
            givePointsStage.setTitle("Give Points");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.show();

            App.appContextHolder.getHomeStage().close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void logoutEmployee() {
        //cleanr AppContextHolder
        App.appContextHolder.setEmployeeId(null);
        App.appContextHolder.setEmployeeName(null);
        App.appContextHolder.setCustomerMobile(null);
        //redirect to login page
        Stage stage = new Stage();
        stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
        stage.setMaximized(true);
        stage.show();
        App.appContextHolder.getHomeStage().close();
        App.appContextHolder.setHomeStage(stage);
    }

    public void fetchCustomerData(Object callbackfunction) {
        String result = "";
        if (App.appContextHolder.getCustomerMobile() != null && App.appContextHolder.getCustomerUUID() != null) {

            try {
                //Build request body
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, App.appContextHolder.getCustomerMobile()));

                String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
                result = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(result);
                JSONObject data = (JSONObject) jsonResponse.get("data");
                //get current points
                params = new ArrayList<>();
                url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                JSONObject json = (JSONObject) parser.parse(result);
                data.put("points",  json.get("data"));
                //get member rewards
                url = baseUrl + customerRewardsEndpoint.replace(":id",App.appContextHolder.getCustomerUUID());
                String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
                JSONObject j = (JSONObject) parser.parse(responseStr);
                List<JSONObject> d = (ArrayList) j.get("data");
                responseStr = new Gson().toJson(d);
                data.put("activeVouchers", responseStr);
                result = jsonResponse.toJSONString();
                final String finalData = result;
                new Thread( () -> {
                    Java2JavascriptUtils.call(callbackfunction, finalData);
                }).start();
            } catch(IOException e) {
                App.appContextHolder.setOnlineMode(false);
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    public void getCustomerRewards(Object callbackfunction) {
        try {
            List params = new ArrayList<>();
            String url = baseUrl + customerRewardsEndpoint.replace(":id",App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseStr);
            List<JSONObject> data = (ArrayList) jsonObject.get("data");
            responseStr = new Gson().toJson(data);

            final String dataStr = responseStr;
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, dataStr);
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
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
            String url = baseUrl + customerTransactionsEndpoint.replace(":id",App.appContextHolder.getCustomerUUID());
            String responseStr = apiService.call(url, params, "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);

            webEngine.executeScript("closeLoadingModal()");
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackfunction, responseStr);
            }).start();
        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
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
                    String[] arr = line.split(",");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("mobileNumber", arr[0].split("=")[1]);
                    jsonObject.put("totalAmount", arr[1].split("=")[1]);
                    jsonObject.put("orNumber", arr[2].split("=")[1]);
                    jsonObject.put("date", arr[3].split("=")[1]);
                    jsonArray.add(jsonObject);
                }
            } catch (IOException e) {
                App.appContextHolder.setOnlineMode(false);
                e.printStackTrace();
            }

            final String finalData = jsonArray.toJSONString();
            new Thread( () -> {
                Java2JavascriptUtils.call(callbackFunction, finalData);
            }).start();
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
                    String[] arr = line.split(",");

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

                    String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
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
                        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmount));
                        url = baseUrl + givePointsEndpoint.replace(":customer_uuid", (String) data.get("id"));
                        resultJson = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                        jsonObject = (JSONObject) parser.parse(resultJson);
                        if (!jsonObject.get("error_code").equals("0x0")) {
                            JSONObject error = (JSONObject) jsonObject.get("errors");
                            String errorMessage = "";
                            if (error.get("or_no") != null) {
                                List<String> l = (ArrayList<String>) error.get("or_no");
                                errorMessage = l.get(0);
                            }
                            if (error.get("amount") != null) {
                                List<String> l = (ArrayList<String>) error.get("amount");
                                errorMessage = l.get(0);
                            }
                            json.put("message", errorMessage);
                            failedArray.add(json);
                        } else {
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
                this.webEngine.executeScript("sendOfflinePointsResponse('"+ finalJson.toJSONString()+"')");
            } catch (IOException e) {
                App.appContextHolder.setOnlineMode(false);
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
        }
    }
}
