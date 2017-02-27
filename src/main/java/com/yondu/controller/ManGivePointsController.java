package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.Account;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.RouteService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_DETAILS_FXML;

/**
 * Created by erwin on 10/14/2016.
 */
public class ManGivePointsController implements Initializable{

    @FXML
    public Button ocrBtn;
    @FXML
    public Button homeBtn;
    @FXML
    public javafx.scene.control.Button givePointsBtn;
    @FXML
    public TextField orField;
    @FXML
    public TextField amountField;
    @FXML
    public TextField mobileField;
    @FXML
    public Label mode;

    private RouteService routeService = new RouteService();
    private Stage currentStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentStage = (Stage) ocrBtn.getScene().getWindow();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        if (App.appContextHolder.getWithVk() != null && !App.appContextHolder.getWithVk()) {
            //Bind virtual keyboard shit
            orField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                        FXVK.detach();
                }
            });
            amountField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                        FXVK.detach();
                }
            });
            mobileField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                        FXVK.detach();
                }
            });
        }


        if (!App.appContextHolder.isOnlineMode()) {
            mode.setVisible(true);
            mode.setText("(OFFLINE)");
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

        amountField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    amountField.setText(newValue.replaceAll("[^\\d{1,3}(,?\\d{3})?(\\.\\d{2})?$]", ""));
                }
            }
        });

        this.homeBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            //Open home stage
            if (App.appContextHolder.getEmployeeId() == null ||
                    (App.appContextHolder.getEmployeeId() != null && App.appContextHolder.getEmployeeId().equals("OFFLINE_EMPLOYEE"))) {
                routeService.goToSplashScreen(currentStage);
            } else {
                routeService.goToHomeScreen(currentStage);
            }

            ((Stage) homeBtn.getScene().getWindow()).close();
        });

        this.ocrBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            routeService.goToGivePointsScreen(currentStage);
        });

        this.givePointsBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            String orStr = orField.getText();
            String sales = amountField.getText().replace(",", "");
            String mobile = mobileField.getText();
            Account customer = new Account();
            customer.setMobileNumber(mobile);

            if (mobileField.getText() == null || mobileField.getText().length() != 11) {
                Alert alert = new Alert(Alert.AlertType.ERROR, mobile + " is an invalid mobile number.", ButtonType.OK);
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.showAndWait();
                return;
            }

            try {
                Double totalSales = Double.parseDouble(sales);

                DecimalFormat formatter = new DecimalFormat("#,###.00");
                sales = formatter.format(totalSales);

                Stage stage = new Stage();
                FXMLLoader  loader  = new FXMLLoader(App.class.getResource(GIVE_POINTS_DETAILS_FXML));
                PointsDetailsController pointsDetailsController = new PointsDetailsController(orStr, sales, "", customer);
                loader.setController(pointsDetailsController);
                stage.setScene(new Scene(loader.load(), 600,500));
                stage.resizableProperty().setValue(Boolean.FALSE);
                stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
              //  App.appContextHolder.setPreviousStage("GIVE_POINTS_MANUAL");
                stage.show();

                ((Stage)ocrBtn.getScene().getWindow()).close();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, sales + " is not a valid amount.", ButtonType.OK);
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
    }
}
