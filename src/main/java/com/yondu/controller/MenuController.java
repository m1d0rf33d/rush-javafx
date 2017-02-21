package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/7/17.
 */
public class MenuController implements Initializable {

    @FXML
    public VBox bodyStackPane;
    @FXML
    public Button registerButton;
    @FXML
    public Button memberInquiryButton;
    @FXML
    public Button givePointsButton;
    @FXML
    public VBox rootVBox;
    @FXML
    public Button transactionsButton;
    @FXML
    public Button redeemRewardsButton;
    @FXML
    public Button payWithPointsButton;
    @FXML
    public Button issueRewardsButton;
    @FXML
    public Button offlineButton;
    @FXML
    public Button ocrButton;

    @FXML
    public ScrollPane rootScrollPane;
    @FXML
    public VBox sideBarVBox;

    private RouteService routeService = new RouteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);




        App.appContextHolder.setRootVBox(rootVBox);
        App.appContextHolder.setRootStackPane(bodyStackPane);


        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setAppState(AppState.REGISTRATION);
            highlight(registerButton);
            routeService.loadContentPage(bodyStackPane, REGISTER_SCREEN);
        });

        memberInquiryButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setAppState(AppState.MEMBER_INQUIRY);
            highlight(memberInquiryButton);
            if (App.appContextHolder.getCustomerMobile() == null) {
                routeService.loadContentPage(bodyStackPane, MEMBER_INQUIRY_SCREEN);
            } else {
                disableMenu();
                routeService.loadMemberDetailsScreen();
            }
        });

        transactionsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setAppState(AppState.TRANSACTIONS);
            highlight(transactionsButton);
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(TRANSACTIONS_SCREEN);
            } else {
                disableMenu();
                routeService.loadTransactionsScreen();
            }
        });
        givePointsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            disableMenu();
            highlight(givePointsButton);
            App.appContextHolder.setAppState(AppState.EARN_POINTS);
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(EARN_POINTS_SCREEN);
            } else {
                routeService.loadEarnPointsScreen();
            }
        });

        redeemRewardsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            highlight(redeemRewardsButton);
            disableMenu();
            App.appContextHolder.setAppState(AppState.REDEEM_REWARDS);
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(REDEEM_REWARDS_SCREEN);
            } else {
                routeService.loadRedeemRewardsScreen();
            }

        });

        payWithPointsButton.setOnMouseClicked((MouseEvent e) -> {
            highlight(payWithPointsButton);
            disableMenu();
            App.appContextHolder.setAppState(AppState.PAY_WITH_POINTS);
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(PAY_WITH_POINTS);
            } else {
               routeService.loadPayWithPoints();
            }
        });

        issueRewardsButton.setOnMouseClicked((MouseEvent e) -> {
            highlight(issueRewardsButton);
            disableMenu();
            App.appContextHolder.setAppState(AppState.ISSUE_REWARDS);
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(ISSUE_REWARDS_SCREEN);
            } else {
                routeService.loadIssueRewardsScreen();
            }
        });

        offlineButton.setOnMouseClicked((MouseEvent e) -> {
            highlight(offlineButton);
            routeService.loadOfflineTransactionScreen();
        });

        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            highlight(ocrButton);
            routeService.loadOCRScreen();
        });
    }



    private void loadMobileLoginDialog(String targetScreen) {
        try {
            disableMenu();

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MOBILE_LOGIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 600,400);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(rootVBox.getScene().getWindow());
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
    }
    public void disableMenu() {
        rootVBox.setOpacity(.50);
        for (Node n : rootVBox.getChildren()) {
            n.setDisable(true);
        }
    }
    public void enableMenu() {
        rootVBox.setOpacity(1);
        for (Node n : rootVBox.getChildren()) {
            n.setDisable(false);
        }
    }
    private void highlight (Button button) {
        List<Node> nodes = sideBarVBox.getChildren();
        for (Node n : nodes) {
            if (n instanceof Button) {
                Button b = (Button) n;
                b.getStyleClass().remove("sidebar-selected");
            }
        }

        button.getStyleClass().add("sidebar-selected");
    }

}
