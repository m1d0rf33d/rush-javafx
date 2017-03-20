package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.LoginController;
import com.yondu.controller.LoginOnlineController;
import com.yondu.controller.MenuController;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Merchant;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.ResizeHelper;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/1/17.
 *
 * This is a service guys. Peace out :)
 */
public class RouteService extends BaseService{


    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void goToLoginScreen(Stage currentStage) {
        try {

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root, 1000,700));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource(AppConfigConstants.R_LOGO).toExternalForm()));
            Scene scene  = stage.getScene();
            scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
            stage.show();
            LoginController controller = fxmlLoader.getController();
            controller.initAfterLoad();

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/menu.fxml"));
            Parent root = fxmlLoader.load();
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
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.01)
            );
            pause.setOnFinished(event -> {
                        MenuController menuController = fxmlLoader.getController();
                        menuController.initAfterLoad();
            });
            pause.play();

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
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            this.loadContentPage(bodyStackPane, REDEEM_REWARDS_SCREEN);

        });
        pause.play();
    }
    public void loadEarnPointsScreen() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            String page;
            Merchant merchant = App.appContextHolder.getMerchant();
            if (merchant.getMerchantClassification().equals("BASIC")) {
                page = EARN_POINTS_SCREEN;
            } else {
                page = SG_EARN_POINTS_SCREEN;
            }
            this.loadContentPage(bodyStackPane, page);

        });
        pause.play();
    }
    public void loadPayWithPoints() {
        VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
        this.loadContentPage(bodyStackPane, PAY_WITH_POINTS);
    }
    public void loadGuestPurchase() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            this.loadContentPage(bodyStackPane, GUEST_PURCHASE_SCREEN);
            enableMenu();
        });
        pause.play();
    }

    public void loadIssueRewardsScreen() {
     //   disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {

            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            this.loadContentPage(bodyStackPane, ISSUE_REWARDS_SCREEN);
        });
        pause.play();
    }
    public void loadGiveStampsScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {

            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            this.loadContentPage(bodyStackPane, GIVE_STAMPS_SCREEN);
        });
        pause.play();
    }
    public void loadMemberDetailsScreen(boolean fromInquiry) {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Merchant merchant = App.appContextHolder.getMerchant();
            String screen = "";
            if (merchant.getMerchantClassification().equals("BASIC")) {
                screen = MEMBER_DETAILS_SCREEN;
            } else {
                screen = SG_MEMBER_DETAILS_FXML;
            }
            if (!fromInquiry) {
                ApiResponse apiResponse = memberDetailsService.loginCustomer(App.appContextHolder.getCustomer().getMobileNumber(), App.appContextHolder.getCurrentState());
                if (!apiResponse.isSuccess()) {
                    showPrompt(apiResponse.getMessage(), "MEMBER INQUIRY");
                } else {
                    VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");

                    this.loadContentPage(bodyStackPane, screen);
                }
            } else {
                VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
                this.loadContentPage(bodyStackPane, screen);
            }
        });
        pause.play();

    }
    public void loadMemberDetailsScreen(String mobileNumber) {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
            this.loadContentPage(bodyStackPane, MEMBER_DETAILS_SCREEN);

            ApiResponse apiResponse = memberDetailsService.loginCustomer(mobileNumber, App.appContextHolder.getCurrentState());
            if (!apiResponse.isSuccess()) {
                notifyError(apiResponse.getMessage());
            }
            enableMenu();
        });
        pause.play();
    }

    public void loadTransactionsScreen() {
        VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
        this.loadContentPage(bodyStackPane, TRANSACTIONS_SCREEN);
    }

    public void loadOfflineTransactionScreen() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
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
            VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
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
