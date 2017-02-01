package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

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


    private Properties prop = new Properties();

    public ActivationController() {
       try {
           InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
           if (inputStream != null) {
               prop.load(inputStream);
               inputStream.close();
           } else {
               throw new FileNotFoundException("property file api.properties not found in the classpath");
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rushLogo.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        activateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            activate();
        });
    }
    private void activate() {
        try {
            String url = prop.getProperty("cms_url") + prop.getProperty("tomcat_port") + prop.getProperty("oauth_endpoint");

            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", prop.getProperty("oauth_secret"));
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
            JSONObject jsonObj1 = (JSONObject) parser.parse(result.toString());
            String token = (String) jsonObj1.get("access_token");

            String inputKey = merchantKey.getText();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uniqueKey", inputKey);
            httpClient = HttpClientBuilder.create().build();

            url = prop.getProperty("cms_url") + prop.getProperty("tomcat_port") + prop.getProperty("validate_merchant_endpoint");
            httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(jsonObject.toJSONString());
            httpPost.addHeader("content-type", "application/json");
            httpPost.addHeader("authorization", "Bearer " + token);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            httpClient.close();
            String jsonResponse = result.toString();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            if (jsonObj.get("responseCode").equals("200")) {
                File file = new File(System.getProperty("user.home") + AppConfigConstants.ACTIVATION_LOCATION);
                PrintWriter writer = new PrintWriter(file);
                writer.write("merchant=" + inputKey);
                writer.flush();
                writer.close();

                JSONObject data = (JSONObject) jsonObj.get("data");
                App.appContextHolder.setAppKey((String) data.get("merchantApiKey"));
                App.appContextHolder.setAppSecret((String) data.get("merchantApiSecret"));
                App.appContextHolder.setCustomerAppKey((String) data.get("customerApiKey"));
                App.appContextHolder.setCustomerAppSecret((String) data.get("customerApiSecret"));

                goToLoginPage();
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
    private void goToLoginPage() {
        ((Stage) activateBtn.getScene().getWindow()).close();
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
            stage.setScene(new Scene(root, 600,400));
            stage.resizableProperty().setValue(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
