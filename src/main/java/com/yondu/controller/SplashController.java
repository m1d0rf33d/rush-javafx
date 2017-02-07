package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import com.yondu.service.RouteService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/** Splash Stage/Screen Controller mapped to splash.xml
 *
 *  @author m1d0rf33d
 */
public class SplashController implements Initializable{

    @FXML
    public Label progressStatus;
    @FXML
    public ImageView rushLogoImage;

    private RouteService routeService = new RouteService();
    private ApiService apiService     = new ApiService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.rushLogoImage.setImage(new Image(App.class.getResource(AppConfigConstants.RUSH_LOGO).toExternalForm()));

        InitService initService = new InitService();
        initService.setOnSucceeded((WorkerStateEvent t) -> {
            Stage currentStage = ((Stage) rushLogoImage.getScene().getWindow());
            if (App.appContextHolder.isActivated()) {
                if (App.appContextHolder.isOnlineMode()) {
                   routeService.goToLoginScreen(currentStage);
                   // routeService.goToMenuScreen(currentStage);
                } else {
                    routeService.goToOfflineScreen(currentStage);
                }
            } else {
                routeService.goToActivationScreen(currentStage);
            }
        });
        progressStatus.textProperty().bind(initService.messageProperty());
        initService.start();
    }



    private class InitService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() {

                    try {
                        //Prepare configuration files
                       File dir = new File(System.getenv("RUSH_HOME"));
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        //Offline transactions database
                        File offlineFile = new File(System.getenv("RUSH_HOME") + DIVIDER + AppConfigConstants.OFFLINE_TRANSACTION_FILE);
                        if (!offlineFile.exists()) {
                            offlineFile.createNewFile();
                        }
                        //OCR settings
                        File file = new File(System.getenv("RUSH_HOME") + DIVIDER + OCR_PROPERTIES);
                        if (!file.exists()) {
                            file.createNewFile();
                            PrintWriter fstream = new PrintWriter(new FileWriter(file));
                            fstream.println("sales_pos_x=");
                            fstream.println("sales_pos_y=");
                            fstream.println("sales_width=");
                            fstream.println("sales_height=");
                            fstream.println("or_pos_x=");
                            fstream.println("or_pos_y=");
                            fstream.println("or_width=");
                            fstream.println("or_height=");
                            fstream.flush();
                            fstream.close();
                        }
                        //check if api.properties is present -> Fix the stupid security issue i had on the updater :(
                        File apiProp = new File(System.getenv("RUSH_HOME") +  DIVIDER + API_PROPERTIES);
                        if(apiProp.exists()) {
                            apiProp.delete();
                        }

                        loadEndpointsFromConfig();

                        file = new File(System.getenv("RUSH_HOME") + DIVIDER + ACTIVATION_FILE);
                        if (file.exists()) {
                            App.appContextHolder.setActivated(true);
                            loadMerchantKeys();
                            App.appContextHolder.setOnlineMode(true);
                        } else {
                            App.appContextHolder.setActivated(false);
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        App.appContextHolder.setOnlineMode(false);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
    }

    private void loadMerchantKeys() throws IOException, ParseException {

        String merchantKey = this.getMerhant();
        JSONObject payload = new JSONObject();
        payload.put("uniqueKey", merchantKey);

        String url = CMS_URL + TOMCAT_PORT + VALIDATE_MERCHANT_ENDPOINT;
        JSONObject jsonObject = apiService.callWidgetAPI(url, payload, "post");
        if (jsonObject != null) {
            JSONObject data = (JSONObject) jsonObject.get("data");
            MERCHANT_APP_KEY = (String) data.get("merchantApiKey");
            MERCHANT_APP_SECRET = (String) data.get("merchantApiSecret");
            CUSTOMER_APP_KEY =(String) data.get("customerApiKey");
            CUSTOMER_APP_SECRET = (String) data.get("customerApiSecret");
        }

    }

    private String getMerhant() {
       try {
           File file = new File(System.getenv("RUSH_HOME") + DIVIDER + ACTIVATION_FILE);
           BufferedReader br = new BufferedReader(new FileReader(file));
           String line;
           String merchant = null;
           while ((line = br.readLine()) != null) {
               String[] arr = line.split("=");
               merchant = arr[1];
           }
           br.close();
           return merchant;
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
       return null;
    }


    private void loadEndpointsFromConfig() {
       try {
           Properties prop = new Properties();
           InputStream inputStream = getClass().getClassLoader().getResourceAsStream(API_PROPERTIES);
           if (inputStream != null) {
               prop.load(inputStream);
               inputStream.close();
           } else {
               throw new FileNotFoundException("property file api.properties not found in the classpath");
           }
           BASE_URL = prop.getProperty("base_url");
           REGISTER_ENDPOINT = prop.getProperty("register_endpoint");
           MEMBER_LOGIN_ENDPOINT = prop.getProperty("member_login_endpoint");
           POINTS_CONVERSION_ENDPOINT = prop.getProperty("points_conversion_endpoint");
           GIVE_POINTS_ENDPOINT = prop.getProperty("give_points_endpoint");
           GET_POINTS_ENDPOINT = prop.getProperty("get_points_endpoint");
           PAY_WITH_POINTS_ENDPOINT = prop.getProperty("pay_points_endpoint");
           GET_REWARDS_ENDPOINT = prop.getProperty("get_rewards_endpoint");
           REDEEM_REWARDS_ENDPOINT = prop.getProperty("redeem_rewards_endpoint");
           UNCLAIMED_REWARDS_ENDPOINT = prop.getProperty("unclaimed_rewards_endpoint");
           CLAIM_REWARDS_ENDPOINT = prop.getProperty("claim_rewards_endpoint");
           GET_REWARDS_MERCHANT_ENDPOINT = prop.getProperty("get_rewards_merchant_endpoint");
           CUSTOMER_REWARDS_ENDPOINT = prop.getProperty("customer_rewards_endpoint");
           CUSTOMER_TRANSACTION_ENDPOINT = prop.getProperty("customer_transactions_endpoint");
           GET_BRANCHES_ENDPOINT = prop.getProperty("get_branches_endpoint");
           LOGIN_ENDPOINT = prop.getProperty("login_endpoint");
           AUTHORIZATION_ENDPOINT = prop.getProperty("authorization_endpoint");
           MERCHANT_DESIGNS_ENDPOINT = prop.getProperty("merchant_designs_endpoint");
           MERCHANT_SETTINGS_ENDPOINT = prop.getProperty("merchant_settings_endpoint");
           EARN_GUEST_ENDPOINT = prop.getProperty("earn_guest_endpoint");

           CMS_URL = prop.getProperty("cms_url");
           TOMCAT_PORT = prop.getProperty("tomcat_port");
           OAUTH_SECRET = prop.getProperty("oauth_secret");
           OAUTH_ENDPOINT = prop.getProperty("oauth_endpoint");
           VALIDATE_MERCHANT_ENDPOINT = prop.getProperty("validate_merchant_endpoint");
           ACCESS_ENDPOINT = prop.getProperty("access_endpoint");
       } catch(IOException e) {
           e.printStackTrace();
       }
    }
}
