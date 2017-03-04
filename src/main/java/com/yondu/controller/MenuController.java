package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import com.yondu.service.CommonService;
import com.yondu.service.MenuService;
import com.yondu.service.RouteService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    public Button offlineButton;
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

    private RouteService routeService = new RouteService();
    private CommonService commonService = new CommonService();
    private ContextMenu contextMenu = new ContextMenu();

    private MenuService menuService = new MenuService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);

        menuService.initialize();
        bindRightClick();
        bindLogout();


        rootVBox.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(rootVBox, e.getScreenX(), e.getScreenY());
            } else {
                contextMenu.hide();
            }
        });


        App.appContextHolder.setRootContainer(rootVBox);

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
                routeService.loadMemberDetailsScreen();
            }
        });

        transactionsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.TRANSACTIONS);
            commonService.updateButtonState();
            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
                disableMenu();
                routeService.loadTransactionsScreen();
            }
        });
        givePointsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            disableMenu();
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.EARN_POINTS);
            commonService.updateButtonState();
            if (App.appContextHolder.getCustomer() == null) {
                loadMobileLoginDialog();
            } else {
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

        offlineButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.OFFLINE);
            commonService.updateButtonState();
            routeService.loadOfflineTransactionScreen();
        });

        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getCurrentState());
            App.appContextHolder.setCurrentState(AppState.OCR);
            commonService.updateButtonState();
            routeService.loadOCRScreen();
        });
    }
    private void bindLogout() {
        employeeMenuButton.getItems().clear();


        MenuItem logoutMenuItem = new MenuItem();
        logoutMenuItem.setGraphic(new Label("LOGOUT"));
        logoutMenuItem.getStyleClass().add("menuitem");
        logoutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                App.appContextHolder.setEmployee(null);
                App.appContextHolder.setCustomer(null);
                routeService.goToLoginScreen((Stage) rootVBox.getScene().getWindow());
            }
        });
        employeeMenuButton.getItems().addAll(logoutMenuItem);
    }

    private void bindRightClick() {
        MenuItem menuItem = new MenuItem("Reload");
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                menuService.initialize();
            }
        });
        contextMenu.getItems().add(menuItem);
    }

    private void loadMobileLoginDialog() {
        try {
            disableMenu();

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MOBILE_LOGIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 500,300);
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

}
