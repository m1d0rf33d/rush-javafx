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
public class RouteService extends BaseService{

    private VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    public void goToLoginScreen(Stage currentStage) {
        try {

            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.LOGIN_FXML));
            stage.setScene(new Scene(root, 1000,700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));

            stage.show();
            stage.setMaximized(true);

            if (currentStage != null) {
                currentStage.close();
            }

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

    public FXMLLoader loadContentPage(VBox bodyStackPane, String page) {

        try {
            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(page));

            Parent root = fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
            return fxmlLoader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void loadRedeemRewardsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, REDEEM_REWARDS_SCREEN);

            String customerUUID = App.appContextHolder.getCustomer().getUuid();
            ApiResponse apiResponse = memberDetailsService.loginCustomer(customerUUID);
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }
    public void loadEarnPointsScreen() {
        this.loadContentPage(bodyStackPane, EARN_POINTS_SCREEN);
    }
    public void loadPayWithPoints() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {

            this.loadContentPage(bodyStackPane, PAY_WITH_POINTS);
            Customer customer = App.appContextHolder.getCustomer();
            ApiResponse apiResponse  = memberDetailsService.loginCustomer(customer.getUuid());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }
    public void loadGuestPurchase() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, GUEST_PURCHASE_SCREEN);
            enableMenu();
        });
        pause.play();
    }

    public void loadIssueRewardsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, ISSUE_REWARDS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getUuid());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }
    public void loadGiveStampsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, GIVE_STAMPS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getUuid());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
        });
        pause.play();
    }
    public void loadMemberDetailsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, MEMBER_DETAILS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getUuid());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }

    public void loadTransactionsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, TRANSACTIONS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getUuid());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }

    public void loadOfflineTransactionScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, OFFLINE_SCREEN);
            enableMenu();
        });
        pause.play();
    }
    public void loadOCRScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            this.loadContentPage(bodyStackPane, OCR_SCREEN);
            enableMenu();
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

    public void loadPinScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            try {
                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 500,300);
                stage.setScene(scene);
                stage.setTitle(APP_TITLE);
                stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
                stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        enableMenu();
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
        alert.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
        alert.setHeaderText("REGISTER MEMBER");
        alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }
}
