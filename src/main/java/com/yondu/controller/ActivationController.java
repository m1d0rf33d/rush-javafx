package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by erwin on 10/11/2016.
 */
public class ActivationController implements Initializable{

    @FXML
    public Button activateBtn;
    @FXML
    public TextField merchantKey;
    @FXML
    public ImageView rushLogo;

    private RouteService routeService = new RouteService();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rushLogo.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        activateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            activate();
        });
    }
    private void activate() {
        try {

            String token = this.getOauthKey();

            String inputKey = merchantKey.getText();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uniqueKey", inputKey);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            String url = CMS_URL + TOMCAT_PORT + VALIDATE_MERCHANT_ENDPOINT;
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(jsonObject.toJSONString());
            httpPost.addHeader("content-type", "application/json");
            httpPost.addHeader("authorization", "Bearer " + token);
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            httpClient.close();

            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(result.toString());
            if (jsonResponse.get("responseCode").equals("200")) {
                File file = new File(System.getenv("RUSH_HOME") + DIVIDER + ACTIVATION_FILE);
                PrintWriter writer = new PrintWriter(file);
                writer.write("merchant=" + inputKey);
                writer.flush();
                writer.close();

                JSONObject data = (JSONObject) jsonResponse.get("data");
                MERCHANT_APP_KEY = (String) data.get("merchantApiKey");
                MERCHANT_APP_SECRET = (String) data.get("merchantApiSecret");
                CUSTOMER_APP_KEY =(String) data.get("customerApiKey");
                CUSTOMER_APP_SECRET = (String) data.get("customerApiSecret");

                Stage currentStage = ((Stage) activateBtn.getScene().getWindow());
                routeService.goToLoginScreen(currentStage);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid merchant key.");
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.showAndWait();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(true);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String getOauthKey() {
       try {
           String url = CMS_URL + TOMCAT_PORT + OAUTH_ENDPOINT;
           CloseableHttpClient httpClient = HttpClientBuilder.create().build();
           HttpPost httpPost = new HttpPost(url);
           httpPost.addHeader("Authorization", OAUTH_SECRET);
           httpPost.addHeader("Content-Type", "application/json");
           HttpResponse response = httpClient.execute(httpPost);

           BufferedReader rd = new BufferedReader(
                   new InputStreamReader(response.getEntity().getContent()));

           StringBuffer result = new StringBuffer();
           String line;
           while ((line = rd.readLine()) != null) {
               result.append(line);
           }
           JSONParser parser = new JSONParser();
           JSONObject jsonResponse = (JSONObject) parser.parse(result.toString());
           return (String) jsonResponse.get("access_token");
       } catch (ParseException e) {
           e.printStackTrace();
       } catch (ClientProtocolException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
        return null;
    }


}
