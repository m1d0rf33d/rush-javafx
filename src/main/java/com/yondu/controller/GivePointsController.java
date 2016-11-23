package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
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
    public Button homeBtn;
    @FXML
    public Button givePointsButton;
    @FXML
    public TextField mobileField;
    @FXML
    public Label mode;
    @FXML
    public Button manualBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        if (App.appContextHolder.getWithVk() != null && !App.appContextHolder.getWithVk()) {
            mobileField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
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
                try {
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
            } else {
                Stage stage = new Stage();
                stage.setScene(new Scene(new Browser(),width - 20, height - 70, Color.web("#666970")));
                stage.setTitle("Rush POS Sync");
                stage.setMaximized(true);
                stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.show();
                App.appContextHolder.setHomeStage(stage);
            }

            ((Stage) homeBtn.getScene().getWindow()).close();
        });

        this.givePointsButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent t) -> {
            givePoints();
        });

        this.manualBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
           try {
               Stage givePointsStage = new Stage();
               Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/give-points-manual.fxml"));
               givePointsStage.setScene(new Scene(root, 500,300));

               givePointsStage.setTitle("Rush POS Sync");
               givePointsStage.resizableProperty().setValue(Boolean.FALSE);
               givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
               givePointsStage.show();
               ((Stage) manualBtn.getScene().getWindow()).close();
           } catch (IOException e) {
               e.printStackTrace();
           }
        });

    }

    private void givePoints() {
        try {
            if (App.appContextHolder.getEmployeeId().equals("OFFLINE_EMPLOYEE")) {
                throw new IOException();
            }

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileField.getText()));
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String responseStr = App.appContextHolder.getApiService().call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
            if (!(jsonResponse.get("error_code")).equals("0x0")) {
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION,(String) "Invalid mobile number", ButtonType.OK);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.OK) {
                    alert.close();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private  void loadGivePointsDetailsView() {
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
