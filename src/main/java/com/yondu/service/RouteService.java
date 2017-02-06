package com.yondu.service;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.AppConfigConstants;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;

/**
 * Created by lynx on 2/1/17.
 *
 * This is a service guys. Peace out :)
 */
public class RouteService {

    private Double screenWidth;
    private Double screenHeight;

    public RouteService() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.getWidth();
        screenHeight = screenSize.getHeight();
    }

    public void goToLoginScreen(Stage currentStage) {
        try {

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.LOGIN_FXML));
            stage.setScene(new Scene(root, 1000,700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));
            stage.show();
            currentStage.close();
            App.appContextHolder.setHomeStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToActivationScreen(Stage currentStage) {
        try {
            //Launch activation window
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.ACTIVATION_FXML));
            stage.setScene(new Scene(root, 800,500));
            stage.setTitle(APP_TITLE);
            stage.resizableProperty().setValue(Boolean.FALSE);
            stage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));
            stage.show();
            currentStage.close();
            App.appContextHolder.setHomeStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToOfflineScreen(Stage currentStage) {
        try {
            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.GIVE_POINTS_MANUAL_FXML));
            givePointsStage.setScene(new Scene(root, 500,300));
            givePointsStage.setTitle(APP_TITLE);
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));
            givePointsStage.show();
            currentStage.close();
            App.appContextHolder.setHomeStage(currentStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToSplashScreen(Stage currentStage) {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
            primaryStage.setScene(new Scene(root, 600,400));
            primaryStage.resizableProperty().setValue(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            primaryStage.show();
            currentStage.close();
            App.appContextHolder.setHomeStage(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToHomeScreen(Stage currentStage) {
        Stage stage = new Stage();
        stage.setScene(new Scene(new Browser(),screenWidth - 20, screenHeight - 70, javafx.scene.paint.Color.web("#666970")));
        stage.setTitle(APP_TITLE);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
        stage.show();
        currentStage.close();
        App.appContextHolder.setHomeStage(stage);
    }

    public void goToManualGivePointsScreen(Stage currentStage) {
        try {
            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/give-points-manual.fxml"));
            givePointsStage.setScene(new Scene(root, 500,300));
            givePointsStage.setTitle(APP_TITLE);
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();
            currentStage.close();
            App.appContextHolder.setHomeStage(givePointsStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToGivePointsScreen(Stage currentStage) {
        try {
            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.GIVE_POINTS_FXML));
            givePointsStage.setScene(new Scene(root, 400, 220));

            givePointsStage.setTitle(APP_TITLE);
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToGivePointsManualScreen(Stage currentStage) {
        try {
            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/give-points-manual.fxml"));
            givePointsStage.setScene(new Scene(root, 500, 300));

            givePointsStage.setTitle(APP_TITLE);
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
