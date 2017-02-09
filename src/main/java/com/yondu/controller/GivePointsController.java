package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.LOADING_FXML;
import static com.yondu.AppContextHolder.*;

/**
 * Created by erwin on 10/2/2016.
 */
public class GivePointsController implements Initializable {

    @FXML
    public Button homeBtn;
    @FXML
    public Button givePointsButton;
    @FXML
    public TextField mobileField;
    @FXML
    public Label mode;
    @FXML
    public Button manualBtn;

    private ApiService apiService                   = new ApiService();
    private RouteService routeService               = new RouteService();
    private NotificationService notificationService = new NotificationService();
    private Stage currentStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentStage = (Stage) homeBtn.getScene().getWindow();

        if (App.appContextHolder.getWithVk() != null && !App.appContextHolder.getWithVk()) {
            mobileField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                        FXVK.detach();
                }
            });
        }

        if (App.appContextHolder.isOnlineMode()) {
            manualBtn.setVisible(false);
        }

        mobileField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    mobileField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (mobileField.getText().length() > 11) {
                    String s = mobileField.getText().substring(0, 11);
                    mobileField.setText(s);
                }
            }
        });
        mobileField.setOnKeyPressed((event)->{
            if(event.getCode() == KeyCode.ENTER) {   givePoints(); }
        });

        if (!App.appContextHolder.isOnlineMode()) {
            mode.setText("(OFFLINE)");
        }
        if (App.appContextHolder.getCustomerMobile() != null) {
            this.mobileField.setText(App.appContextHolder.getCustomerMobile());
        }

        //Add buttons event handlers
        this.homeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if (App.appContextHolder.getEmployeeId() == null ||
                    (App.appContextHolder.getEmployeeId() != null && App.appContextHolder.getEmployeeId().equals("OFFLINE_EMPLOYEE"))) {
                routeService.goToSplashScreen(currentStage);
            } else {
                routeService.goToHomeScreen(currentStage);
            }
        });

        this.givePointsButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent t) -> {
            givePoints();
        });

        this.manualBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            routeService.goToManualGivePointsScreen(currentStage);
        });

    }

    private void givePoints() {
        try {
            if (App.appContextHolder.getEmployeeId().equals("OFFLINE_EMPLOYEE")) {
                throw new IOException();
            }

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getText()));
            String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            if (!(jsonObject.get("error_code")).equals("0x0")) {
                notificationService.showMessagePrompt((String) jsonObject.get("message"), Alert.AlertType.INFORMATION, currentStage, null, ButtonType.OK);
            } else {

                JSONObject data = (JSONObject) jsonObject.get("data");
                App.appContextHolder.setCustomerUUID((String)data.get("id"));
                App.appContextHolder.setCustomerMobile((String) data.get("mobile_no"));
                ((Stage)givePointsButton.getScene().getWindow()).close();
                loadGivePointsDetailsView();
            }
        } catch (IOException e) {
            //Offline mode
            e.printStackTrace();
            //Validate mobile field
            boolean isValid = true;
            if (mobileField.getText().isEmpty()) {
                isValid = false;
            } else {
                if (!NumberUtils.isDigits(mobileField.getText())) {
                    isValid = false;
                }
            }
            if (isValid) {
                App.appContextHolder.setOnlineMode(false);
                App.appContextHolder.setCustomerMobile(mobileField.getText());
                ((Stage)givePointsButton.getScene().getWindow()).close();
                loadGivePointsDetailsView();
            } else {
                notificationService.showMessagePrompt("Invalid mobile number.", Alert.AlertType.INFORMATION, currentStage,null, ButtonType.OK);
            }
        }
    }

    private void loadGivePointsDetailsView() {
        Stage loadingStage = new Stage();
        try {
            Parent root = FXMLLoader.load(App.class.getResource(LOADING_FXML));
            loadingStage.setScene(new Scene(root, 300,100));
            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.resizableProperty().setValue(Boolean.FALSE);
            loadingStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            loadingStage.show();
            App.appContextHolder.setLoadingStage(loadingStage);
            loadingStage.setIconified(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
