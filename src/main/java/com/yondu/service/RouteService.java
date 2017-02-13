package com.yondu.service;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.controller.*;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppConfigConstants;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.IOException;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/1/17.
 *
 * This is a service guys. Peace out :)
 */
public class RouteService {

    private Double screenWidth;
    private Double screenHeight;
    private MenuService menuService = new MenuService();

    public RouteService() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.getWidth();
        screenHeight = screenSize.getHeight();
    }

    public void goToLoginScreen(Stage currentStage) {
        try {

            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.LOGIN_FXML));
            stage.setScene(new Scene(root, 1000,700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));
            stage.show();
            stage.setMaximized(true);
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

    public void goToMenuScreen(Stage currentStage) {
        try {
            Stage stage = new Stage();

            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/menu.fxml"));
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));

            Scene scene = stage.getScene();
            scene.getStylesheets().add(App.class.getResource("/app/css/register.css").toExternalForm());
            stage.show();
            stage.setMaximized(true);
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Loaders


    public FXMLLoader loadContentPage(StackPane bodyStackPane, String page) {
        try {
            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(page));

            Parent root = (Parent)fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
            return fxmlLoader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadRedeemRewardsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), REDEEM_REWARDS_SCREEN);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            RedeemRewardsController redeemRewardsController = fxmlLoader.getController();
            redeemRewardsController.setCustomer(customer);

            jsonObject = menuService.getRewards();
            redeemRewardsController.setRewards((java.util.List< Reward>)jsonObject.get("rewards"));
            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }
    public void loadEarnPointsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), EARN_POINTS_SCREEN);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            EarnPointsController earnPointsController = fxmlLoader.getController();
            earnPointsController.setCustomer(customer);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }
    public void loadPayWithPoints() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), PAY_WITH_POINTS);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            PayWithPointsController payWithPointsController = fxmlLoader.getController();
            payWithPointsController.setCustomer(customer);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }
    public void loadIssueRewardsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), PAY_WITH_POINTS);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            IssueRewardsController issueRewardsController = fxmlLoader.getController();
            issueRewardsController.setCustomer(customer);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }

    public void loadMemberDetailsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), MEMBER_DETAILS_SCREEN);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            MemberDetailsController memberDetailsController = fxmlLoader.getController();
            memberDetailsController.setCustomer(customer);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }

    public void loadTransactionsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), TRANSACTIONS_SCREEN);

            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            Customer customer = (Customer) jsonObject.get("customer");
            TransactionsController transactionsController = fxmlLoader.getController();
            transactionsController.setCustomer(customer);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }

    public void loadOfflineTransactionScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), OFFLINE_SCREEN);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }

}
