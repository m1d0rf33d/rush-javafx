package com.yondu.service;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_FXML;
import static com.yondu.model.constants.AppConfigConstants.SETTINGS_FXML;

/** Home Module services / Java2Javascript bridge
 *  Methods inside this class can be invoked inside a javascript using alert("__CONNECT__BACKEND__homeService")
 *
 *  ITSUMO KOKORO..  yeah..
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

    /** Method name is loadEmployeeData but all needed information on home page including branch and merchant images are already
     * being retrieved on this method. I should probably rename this method but I'm too lazy to do that ryt now..
     *
     * @param callbackfunction
     */
    public void loadEmployeeData(final Object callbackfunction) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-YYYY");
        JSONObject responseObj = new JSONObject();
        responseObj.put("id", App.appContextHolder.getEmployeeId());
        responseObj.put("name",App.appContextHolder.getEmployeeName());
        responseObj.put("currentDate",formatter.format(new Date()));

        //Get branch details too..
        String url = BASE_URL + GET_BRANCHES_ENDPOINT;
        List<NameValuePair> params = new ArrayList<>();
        JSONObject jsonObj = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObj != null) {
            List<JSONObject> data = (ArrayList) jsonObj.get("data");
            for (JSONObject branch : data) {
                if (branch.get("id").equals(App.appContextHolder.getBranchId())) {
                    responseObj.put("branchName", branch.get("name"));
                    responseObj.put("branchLogo", branch.get("logo_url"));
                    break;
                }
            }
        }

        //Get some extra bullshit.. :)
        url = BASE_URL + MERCHANT_DESIGNS_ENDPOINT;
        params = new ArrayList<>();
        jsonObj = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObj != null) {
            JSONObject d = (JSONObject) jsonObj.get("data");
            JSONObject merchant = (JSONObject) d.get("merchant");
            responseObj.put("backgroundUrl", merchant.get("background_url"));
        }

        //Get employee screen access
        url = CMS_URL + TOMCAT_PORT + ACCESS_ENDPOINT;
        jsonObj = apiService.callWidgetAPI(url, new JSONObject(), "get");

        JSONObject dataJson = (JSONObject) jsonObj.get("data");
        List<String> screens = (ArrayList) dataJson.get("access");

        //WithVk means with virtual keyboard yeah that's configurable too..
        Boolean withVk = (Boolean) dataJson.get("withVk");
        App.appContextHolder.setWithVk(withVk);
        responseObj.put("screens", screens);

        new Thread( () -> {
            Java2JavascriptUtils.call(callbackfunction, responseObj.toJSONString());
        }).start();
    }

    /** Register new member, a javascript response handler function will be called to handle the result.
     * I hate it when methods have so many parameters like this wtf? place it on a map or a container object or something bruh?
     *
     */
    public void register(String name,
                         String email,
                         String mobile,
                         String mpin,
                         String birthdate,
                         String gender,
                         Object callbackfunction) {

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

        String url = BASE_URL + REGISTER_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, jsonObject.toJSONString())
            ).start();

            this.webEngine.executeScript("registerResponseHandler('"+jsonObject.toJSONString()+"')");
        }
        webEngine.executeScript("closeLoadingModal('"+ App.appContextHolder.isOnlineMode()+"')");
    }

    /** Pay using points
     *
     */
    public void payWithPoints(String points, String orNumber, String amount, String pin) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumber));
        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount.replace(",","")));
        params.add(new BasicNameValuePair(ApiFieldContants.POINTS, points.replace(",","")));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin.replace(",","")));

        String url = BASE_URL + PAY_WITH_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            webEngine.executeScript("payWithPointsResponse('" + jsonObject.toJSONString() + "')");
        } else {
            //TODO: Offline mode
        }

        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    /** Load all merchant rewards
     *
     * @param callbackfunction
     */
    public void loadRewards(final Object callbackfunction) {

        String url = BASE_URL + GET_REWARDS_MERCHANT_ENDPOINT;
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        new Thread(()->
                Java2JavascriptUtils.call(callbackfunction, jsonObject.toJSONString())
        ).start();
        this.webEngine.executeScript("closeLoadingModal('"+App.appContextHolder.isOnlineMode()+"')");
    }

    /** Redeeem member reward
     *
     * @param rewardId
     * @param pin
     */
    public void redeemRewards(String rewardId, String pin) {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pin));
        String url = BASE_URL + REDEEM_REWARDS_ENDPOINT;
        url = url.replace(":customer_id",App.appContextHolder.getCustomerUUID());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":reward_id", rewardId);
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        this.webEngine.executeScript("redeemRewardsResponseHandler('"+jsonObject.toJSONString()+"')");
    }

    public void loadCustomerRewards(final Object callbackfunction) {
        String url = BASE_URL + GET_REWARDS_ENDPOINT;
        JSONObject jsonObj = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.CUSTOMER_APP_RESOUCE_OWNER);
        List<JSONObject> rewardsDataList = (ArrayList) jsonObj.get("data");

        url = BASE_URL + UNCLAIMED_REWARDS_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":customer_id", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        List<JSONObject> unclaimedDataList = (ArrayList) jsonObject.get("data");
        for (JSONObject unclaimedData : unclaimedDataList) {
            JSONObject reward = (JSONObject) unclaimedData.get("reward");
            String rewardName = (String) reward.get("name");
            for (JSONObject rewardsData: rewardsDataList) {
                String rName = (String) rewardsData.get("name");
                if (rName.equals(rewardName)) {
                    reward.put("details", rewardsData.get("details"));
                    reward.put("image_url", rewardsData.get("image_url"));
                    break;
                }
            }
        }
        new Thread(()->
                Java2JavascriptUtils.call(callbackfunction, jsonObject.toJSONString())
        ).start();
    }


    public void issueReward(String redeemId) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.REDEEM_ID, redeemId));
        String url = BASE_URL + CLAIM_REWARDS_ENDPOINT;
        url = url.replace(":customer_id",App.appContextHolder.getCustomerUUID());
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        this.webEngine.executeScript("issueRewardsResponseHandler('"+jsonObject.toJSONString()+"')");
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
           //
        }
    }

  /*  public void fetchCustomerData(Object callbackfunction) {

        //Get logged in member updated details
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, App.appContextHolder.getCustomerMobile()));

        String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        //Now we get member current points because for some reason it is on a separate API wat the actual fck?
        url = BASE_URL + GET_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
        JSONObject jsonObj = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        String strPoints = (String) jsonObj.get("data");

        //This is the most annoying part. The return data type of points is not consistent sometimes it's with decimals (double) sometimes in long. Can you believe this shit?
        //I mean can't they just use a consistent data type? :)


        if (App.appContextHolder.getCustomerMobile() != null && App.appContextHolder.getCustomerUUID() != null) {

            try {
                DecimalFormat formatter = new DecimalFormat("#,###,###.00");

                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(result);
                JSONObject data = (JSONObject) jsonResponse.get("data");
                //get current points
                params = new ArrayList<>();


                JSONObject json = (JSONObject) parser.parse(result);
                S
                data.put("points",  formatter.format(Double.parseDouble(strPoints)));
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


                Double dPoints = Double.parseDouble(strPoints);
                Double redemptionPeso;
                Long redemptionPoints = (Long) d1.get("redemption_points");
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
           *//* JSONParser parser = new JSONParser();
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
            final String finalData = jsonObj.toJSONString();*//*
            new Thread(()->
                    Java2JavascriptUtils.call(callbackfunction, jsonObj.toJSONString())
            ).start();

        } catch (IOException e) {
            App.appContextHolder.setOnlineMode(false);
            e.printStackTrace();
           
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
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
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
           

        }
    }

    public void loginMember(String mobileNumber, String module) {
        try {
            String result = "";
            String message = "";
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileNumber));

            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String jsonResponse = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject responseJSON = (JSONObject) parser.parse(jsonResponse);
            if (responseJSON.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) responseJSON.get(ApiFieldContants.DATA);
                App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                App.appContextHolder.setCustomerUUID((String) data.get("id"));
                result = "success";
            } else {
                message = (String) responseJSON.get("message");
                result = "failed";
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", result);
            jsonObject.put("mobileNumber", mobileNumber);
            jsonObject.put("message", message);
            jsonObject.put("module", module);
            this.webEngine.executeScript("loginMemberResponseHandler('"+jsonObject.toJSONString()+"')");
        } catch (IOException e) {
            e.printStackTrace();
           
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/


}
