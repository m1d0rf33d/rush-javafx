package com.yondu.service;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.controller.*;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.ResizeHelper;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.*;
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
    private MemberDetailsService memberDetailsService = new MemberDetailsService();
    private RedeemRewardsService redeemRewardsService = new RedeemRewardsService();

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
            scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
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


    public FXMLLoader loadContentPage(VBox bodyStackPane, String page) {

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

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                RedeemRewardsController redeemRewardsController = fxmlLoader.getController();
                redeemRewardsController.setCustomer(customer);

                ApiResponse apiResp = redeemRewardsService.getRewards();
                if (apiResp.isSuccess()) {
                    redeemRewardsController.setRewards((java.util.List< Reward>)apiResp.getPayload().get("rewards"));
                }
            } else {
                notifyError(apiResponse.getMessage());
            }

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

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                EarnPointsController earnPointsController = fxmlLoader.getController();
                earnPointsController.setCustomer(customer);
            } else {
                notifyError(apiResponse.getMessage());
            }
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

            ApiResponse apiResponse  = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                PayWithPointsController payWithPointsController = fxmlLoader.getController();
                payWithPointsController.setCustomer(customer);
            } else {
                notifyError(apiResponse.getMessage());
            }

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
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), ISSUE_REWARDS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                IssueRewardsController issueRewardsController = fxmlLoader.getController();
                issueRewardsController.setCustomer(customer);
                issueRewardsController.setRewards(customer.getActiveVouchers());
            } else {
                notifyError(apiResponse.getMessage());
            }


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

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                MemberDetailsController memberDetailsController = fxmlLoader.getController();
                memberDetailsController.setCustomer(customer);
            } else {
                notifyError(apiResponse.getMessage());
            }


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

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (apiResponse.isSuccess()) {
                Customer customer = (Customer) apiResponse.getPayload().get("customer");
                TransactionsController transactionsController = fxmlLoader.getController();
                transactionsController.setCustomer(customer);
            } else {
                notifyError(apiResponse.getMessage());
            }


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
    public void loadOCRScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            FXMLLoader fxmlLoader = this.loadContentPage(App.appContextHolder.getRootStackPane(), OCR_SCREEN);

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
    }

    public void loadOrCaptureScreen() {
        try {
            Stage orCaptureStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(OR_CAPTURE_FXML));
            orCaptureStage.initStyle(StageStyle.UNDECORATED);
            orCaptureStage.setScene(new Scene(root, 300,100));
            orCaptureStage.setMaxHeight(100);
            orCaptureStage.setMaxWidth(300);
            App.appContextHolder.setOrCaptureStage(orCaptureStage);
            ResizeHelper.addResizeListener(orCaptureStage);
            orCaptureStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            orCaptureStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadSalesCaptureScreen() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(SALES_CAPTURE_FXML));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root, 300,100));
            stage.setMaxHeight(100);
            stage.setMaxWidth(300);
            App.appContextHolder.setSalesCaptureStage(stage);
            ResizeHelper.addResizeListener(stage);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPinScreen(TextField receiptNumber, TextField amount, TextField pointsToPay, javafx.scene.control.Label pointsLabel, Label pesoValueLabel) {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            try {
                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
                Parent root = fxmlLoader.load();
                PinController controller = fxmlLoader.getController();
                controller.setReceiptTextField(receiptNumber);
                controller.setPointsTextField(pointsToPay);
                controller.setAmountTextField(amount);
                controller.setPointsLabel(pointsLabel);
                controller.setPesoValueLabel(pesoValueLabel);
                Scene scene = new Scene(root, 600,400);
                stage.setScene(scene);
                stage.setTitle(APP_TITLE);
                stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
                stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        App.appContextHolder.getRootVBox().setOpacity(1);
                        for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                            n.setDisable(false);
                        }

                    }
                });
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
        pause.play();
    }

    private void notifyError(String message) {
        Text text = new Text(message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(App.appContextHolder.getRootStackPane().getScene().getWindow());
        alert.setHeaderText("REGISTER MEMBER");
        alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }
}
