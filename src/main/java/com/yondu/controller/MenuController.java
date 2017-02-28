package com.yondu.controller;

import com.sun.javafx.scene.control.skin.ContextMenuContent;
import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.ApiService;
import com.yondu.service.CommonService;
import com.yondu.service.RouteService;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.AppContextHolder.ACCESS_ENDPOINT;
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

    private RouteService routeService = new RouteService();
    private ApiService apiService = new ApiService();
    private CommonService commonService = new CommonService();
    private ContextMenu contextMenu = new ContextMenu();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);

        sideBarVBox.getChildren().clear();

        rootVBox.setOpacity(.50);

        loadPage();

        MenuItem menuItem = new MenuItem("Reload");
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                loadPage();
            }
        });
        contextMenu.getItems().add(menuItem);
        rootVBox.setOnMouseClicked((MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(rootVBox, e.getScreenX(), e.getScreenY());
            } else {
                contextMenu.hide();
            }
        });

        employeeMenuButton.setText("Hi! " + App.appContextHolder.getEmployeeName());
        MenuItem logoutMenuItem = new MenuItem();
        logoutMenuItem.setGraphic(new Label("LOGOUT"));
        logoutMenuItem.getStyleClass().add("menuitem");
        logoutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                App.appContextHolder.setEmployeeName(null);
                App.appContextHolder.setEmployeeId(null);
                App.appContextHolder.setCustomerUUID(null);
                App.appContextHolder.setCustomerMobile(null);
                routeService.goToLoginScreen((Stage) rootVBox.getScene().getWindow());
            }
        });
        employeeMenuButton.getItems().clear();
        employeeMenuButton.getItems().addAll(logoutMenuItem);

        App.appContextHolder.setRootVBox(rootVBox);
        App.appContextHolder.setRootStackPane(bodyStackPane);


        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.REGISTRATION);
            commonService.updateButtonState();
            routeService.loadContentPage(bodyStackPane, REGISTER_SCREEN);
        });

        guestPurchaseButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.GUEST_PURCHASE);
            commonService.updateButtonState();
            routeService.loadGuestPurchase();
        });

        memberInquiryButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.MEMBER_INQUIRY);
            commonService.updateButtonState();
            if (App.appContextHolder.getCustomerMobile() == null) {
                routeService.loadContentPage(bodyStackPane, MEMBER_INQUIRY_SCREEN);
            } else {
                disableMenu();
                routeService.loadMemberDetailsScreen();
            }
        });

        transactionsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.TRANSACTIONS);
            commonService.updateButtonState();
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(TRANSACTIONS_SCREEN);
            } else {
                disableMenu();
                routeService.loadTransactionsScreen();
            }
        });
        givePointsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            disableMenu();
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.EARN_POINTS);
            commonService.updateButtonState();
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(EARN_POINTS_SCREEN);
            } else {
                routeService.loadEarnPointsScreen();
            }
        });

        redeemRewardsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.REDEEM_REWARDS);
            commonService.updateButtonState();
            disableMenu();
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(REDEEM_REWARDS_SCREEN);
            } else {
                routeService.loadRedeemRewardsScreen();
            }

        });

        payWithPointsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.PAY_WITH_POINTS);

            commonService.updateButtonState();
            disableMenu();

            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(PAY_WITH_POINTS);
            } else {
               routeService.loadPayWithPoints();
            }
        });

        issueRewardsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.ISSUE_REWARDS);
            commonService.updateButtonState();
            disableMenu();

            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog(ISSUE_REWARDS_SCREEN);
            } else {
                routeService.loadIssueRewardsScreen();
            }
        });

        offlineButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.OFFLINE);
            commonService.updateButtonState();
            routeService.loadOfflineTransactionScreen();
        });

        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.setPrevState(App.appContextHolder.getAppState());
            App.appContextHolder.setAppState(AppState.OCR);
            commonService.updateButtonState();
            routeService.loadOCRScreen();
        });
    }

    private void loadPage() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            loadMerchantDetails();
            enableMenu();
        });
        pause.play();
    }
    private void loadMerchantDetails() {

        if (loadScreenRestrictions()) {
            String url = BASE_URL + GET_BRANCHES_ENDPOINT;
            List<NameValuePair> params = new ArrayList<>();
            JSONObject jsonObj = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            if (jsonObj != null) {
                List<JSONObject> data = (ArrayList) jsonObj.get("data");
                for (JSONObject branch : data) {
                    if (branch.get("id").equals(App.appContextHolder.getBranchId())) {
                        branchNameLabel.setText((String) branch.get("name"));
                        merchantLogoImageView.setImage(new Image((String) branch.get("logo_url")));
                        break;
                    }
                }
            } else {
                showOfflinePrompt();
            }
        }

    }

    private boolean loadScreenRestrictions() {
        sideBarVBox.getChildren().clear();
        //Get employee screen access
        String url = CMS_URL + TOMCAT_PORT + ACCESS_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":branch_id", App.appContextHolder.getBranchId());
        JSONObject jsonObj = apiService.callWidgetAPI(url, new JSONObject(), "get");
        if (jsonObj != null) {
            JSONObject dataJson = (JSONObject) jsonObj.get("data");
            List<String> screens = (ArrayList) dataJson.get("access");
            List<Button> buttons = new ArrayList<>();
            for (String screen : screens) {
                if (screen.equalsIgnoreCase("REGISTER")) {
                    buttons.add(registerButton);
                }
                if (screen.equalsIgnoreCase("MEMBER_PROFILE")) {
                    buttons.add(memberInquiryButton);
                }
                if (screen.equalsIgnoreCase("GIVE_POINTS")) {
                    buttons.add(givePointsButton);
                }
                if (screen.equalsIgnoreCase("GUEST_PURCHASE")) {
                    buttons.add(guestPurchaseButton);
                }
                if (screen.equalsIgnoreCase("OFFLINE_TRANSACTIONS")) {
                    buttons.add(offlineButton);
                }
                if (screen.equalsIgnoreCase("PAY_WITH_POINTS")) {
                    buttons.add(payWithPointsButton);
                }
                if (screen.equalsIgnoreCase("REDEEM_REWARDS")) {
                    buttons.add(redeemRewardsButton);
                }
                if (screen.equalsIgnoreCase("ISSUE_REWARDS")) {
                    buttons.add(issueRewardsButton);
                }

                if (screen.equalsIgnoreCase("TRANSACTIONS_VIEW")) {
                    buttons.add(transactionsButton);
                }

                if (screen.equalsIgnoreCase("OCR_SETTINGS")) {
                    buttons.add(ocrButton);
                }
            }
            sideBarVBox.getChildren().addAll(buttons);
            //WithVk means with virtual keyboard yeah that's configurable too..
            Boolean withVk = (Boolean) dataJson.get("withVk");
            App.appContextHolder.setWithVk(withVk);
        } else {
            showOfflinePrompt();
            return false;
        }
        return true;
    }


    private void loadMobileLoginDialog(String targetScreen) {
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
    private void showOfflinePrompt() {
        Text text = new Text("Network connection error.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(rootVBox.getScene().getWindow());
        alert.setHeaderText("MENU");
        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }

}
