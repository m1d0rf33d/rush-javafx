package com.yondu.controller;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.service.ApiService;
import javafx.event.EventHandler;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
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

import static com.yondu.model.constants.AppConfigConstants.*;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

/**
 * Created by erwin on 10/2/2016.
 */
public class GivePointsController implements Initializable {

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public ImageView homeImageView;
    @FXML
    public Button givePointsButton;
    @FXML
    public TextField mobileField;

    private String baseUrl;
    private String memberLoginEndpoint;
    private ApiService apiService;
    private String givePointsEndpoint;
    private Stage givePointsDetailsStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService();
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            prop.load(inputStream);
            this.baseUrl = prop.getProperty("base_url");
            this.memberLoginEndpoint = prop.getProperty("member_login_endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.rushLogoImageView.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        this.homeImageView.setImage(new Image(App.class.getResource("/app/images/home.png").toExternalForm()));
        if (App.appContextHolder.getCustomerMobile() != null) {
            this.mobileField.setText(App.appContextHolder.getCustomerMobile());
        }

        //Add buttons event handlers
        this.homeImageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Stage stage = new Stage();
                stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
                stage.setMaximized(true);
                stage.show();
                App.appContextHolder.setHomeStage(stage);
                ((Stage) homeImageView.getScene().getWindow()).close();
            }
        });

        this.givePointsButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (App.appContextHolder.getCustomerMobile() == null) {
                    //login member
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getText()));

                    String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
                    String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
                        if (!((String)jsonResponse.get("error_code")).equals("0x0")) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION,(String) jsonResponse.get("message"), ButtonType.OK);
                            alert.showAndWait();

                            if (alert.getResult() == ButtonType.OK) {
                                alert.close();
                            }
                        } else {
                           //Load givepoints result
                            JSONObject data = (JSONObject) jsonResponse.get("data");
                            App.appContextHolder.setCustomerUUID((String)data.get("id"));
                            App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                            ((Stage)givePointsButton.getScene().getWindow()).close();
                            loadGivePointsDetailsView();

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    ((Stage)givePointsButton.getScene().getWindow()).close();
                    loadGivePointsDetailsView();

                }
            }
        });

    }

    private  void loadGivePointsDetailsView() {
        Stage loadingStage = new Stage();
        try {

            Parent root = FXMLLoader.load(App.class.getResource(LOADING_FXML));
            loadingStage.setScene(new Scene(root, 500,300));
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.resizableProperty().setValue(Boolean.FALSE);
            loadingStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
