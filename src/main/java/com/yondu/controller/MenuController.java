package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import com.yondu.service.CommonService;
import com.yondu.service.MenuService;
import com.yondu.service.RouteService;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
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
    public Button ocrButton;
    @FXML
    public ScrollPane rootScrollPane;
    @FXML
    public VBox sideBarVBox;
    @FXML
    public Button guestPurchaseButton;
    @FXML
    public MenuButton employeeMenuButton;
    @FXML
    public Label branchNameLabel;
    @FXML
    public ImageView merchantLogoImageView;
    @FXML
    public Button giveStampsButton;

    private RouteService routeService = App.appContextHolder.routeService;
    private CommonService commonService = App.appContextHolder.commonService;
    private ContextMenu contextMenu = new ContextMenu();

    private MenuService menuService = App.appContextHolder.menuService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    private void loadMobileLoginDialog() {
        try {

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MOBILE_LOGIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 450,200);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.resizableProperty().setValue(Boolean.FALSE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(rootVBox.getScene().getWindow());
            stage.setOnCloseRequest(event -> {
                enableMenu();
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

    public void initAfterLoad() {
        App.appContextHolder.setRootContainer(rootVBox);
        App.appContextHolder.setCurrentState(AppState.MENU);

        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);
        rootScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        for (Node node : sideBarVBox.getChildren()) {
            node.setVisible(false);
        }

        menuService.initialize();

        rootVBox.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(rootVBox, e.getScreenX(), e.getScreenY());
            } else {
                contextMenu.hide();
            }
        });


        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.REGISTRATION);
            commonService.updateButtonState();
            routeService.loadContentPage(bodyStackPane, REGISTER_SCREEN);
        });

        guestPurchaseButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.GUEST_PURCHASE);
            commonService.updateButtonState();
            routeService.loadGuestPurchase();
        });

        memberInquiryButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {

            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.MEMBER_INQUIRY);

            commonService.updateButtonState();
            if (App.appContextHolder.getCustomer() == null) {
                routeService.loadContentPage(bodyStackPane, MEMBER_INQUIRY_SCREEN);
            } else {
                disableMenu();
                App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
                routeService.loadMemberDetailsScreen(false);
            }
        });

        transactionsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.TRANSACTIONS);
            commonService.updateButtonState();
            routeService.loadTransactionsScreen();
        });
        givePointsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            disableMenu();
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.EARN_POINTS);

            commonService.updateButtonState();
            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
                routeService.loadEarnPointsScreen();
            }
        });

        redeemRewardsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.REDEEM_REWARDS);

            commonService.updateButtonState();
            disableMenu();
            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
                routeService.loadRedeemRewardsScreen();
            }

        });

        payWithPointsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.PAY_WITH_POINTS);

            commonService.updateButtonState();
            disableMenu();

            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                routeService.loadPayWithPoints();
            }
        });

        issueRewardsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.ISSUE_REWARDS);
            commonService.updateButtonState();
             disableMenu();

            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                routeService.loadIssueRewardsScreen();
            }
        });

        giveStampsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.GIVE_STAMPS);
            commonService.updateButtonState();
            disableMenu();

            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                routeService.loadGiveStampsScreen();
            }
        });

        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.OCR);
            commonService.updateButtonState();
            routeService.loadOCRScreen();
        });
    }
}
