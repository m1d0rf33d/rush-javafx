package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import java.util.ResourceBundle;
import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by erwin on 10/11/2016.
 */
public class ActivationController implements Initializable{

    @FXML
    public Button activateButton;
    @FXML
    public TextField merchantKey;
    @FXML
    public ImageView logoImageView;
    @FXML
    public Button cancelButton;

    private RouteService routeService               = new RouteService();
    private NotificationService notificationService = new NotificationService();
    private ApiService apiService                   = new ApiService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        logoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));

        activateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            activate();
        });
        cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            closeApp();
        });
    }

    private void closeApp() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void activate() {
        try {
            String inputKey = merchantKey.getText();
            JSONObject payload = new JSONObject();
            payload.put("uniqueKey", inputKey);

            String url = CMS_URL + TOMCAT_PORT + VALIDATE_MERCHANT_ENDPOINT;
            JSONObject jsonObject = apiService.callWidgetAPI(url, payload, "post");
            if (jsonObject.get("responseCode").equals("200")) {
                File file = new File(System.getenv("RUSH_HOME") + DIVIDER + ACTIVATION_FILE);
                PrintWriter writer = new PrintWriter(file);
                writer.write("merchant=" + inputKey);
                writer.flush();
                writer.close();

                JSONObject data = (JSONObject) jsonObject.get("data");
                MERCHANT_APP_KEY = (String) data.get("merchantApiKey");
                MERCHANT_APP_SECRET = (String) data.get("merchantApiSecret");
                CUSTOMER_APP_KEY =(String) data.get("customerApiKey");
                CUSTOMER_APP_SECRET = (String) data.get("customerApiSecret");

                Stage currentStage = ((Stage) activateButton.getScene().getWindow());
                routeService.goToLoginScreen(currentStage);
            } else {
                notificationService.showMessagePrompt("Invalid merchant key.", Alert.AlertType.ERROR, activateButton.getScene().getWindow(), ButtonType.OK);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(true);
        }
    }

}
