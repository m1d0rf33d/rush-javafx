package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Employee;
import com.yondu.model.Merchant;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;
/**
 * Created by lynx on 2/9/17.
 */
public class MenuService extends BaseService{

    private RouteService routeService = App.appContextHolder.routeService;

    public void initialize() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Branch branch = App.appContextHolder.getBranch();

            VBox rootVBox = App.appContextHolder.getRootContainer();
            MenuButton employeeMenuButton = (MenuButton) rootVBox.getScene().lookup("#employeeMenuButton");
            employeeMenuButton.setText("Hi! " + App.appContextHolder.getEmployee().getEmployeeName());
            employeeMenuButton.getItems().clear();

            Label label = new Label("LOGOUT");
            label.setId("logoutLabel");
            MenuItem logoutMenuItem = new MenuItem();
            logoutMenuItem.setGraphic(label);
            logoutMenuItem.getGraphic().setStyle("-fx-min-width: " + employeeMenuButton.getWidth() + "px;");
            logoutMenuItem.getStyleClass().add("menuitem");
            logoutMenuItem.setId("logoutButton");
            logoutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    App.appContextHolder.setEmployee(null);
                    App.appContextHolder.setCustomer(null);
                    App.appContextHolder.routeService.goToLoginScreen((Stage) rootVBox.getScene().getWindow());
                }
            });
            employeeMenuButton.getItems().addAll(logoutMenuItem);

            Label branchNameLabel = (Label) rootVBox.getScene().lookup("#branchNameLabel");
            ImageView merchantLogoImageView = (ImageView) rootVBox.getScene().lookup("#merchantLogoImageView");
            branchNameLabel.setText(branch.getName());

            if (App.appContextHolder.getMerchant() != null && App.appContextHolder.getMerchant().getBackgroundUrl() != null) {
                ImageView imageView = new ImageView(new Image(App.appContextHolder.getMerchant().getBackgroundUrl()));
                VBox bodyStackPane = (VBox) rootVBox.getScene().lookup("#bodyStackPane");
                bodyStackPane.getChildren().clear();
                bodyStackPane.getChildren().add(imageView);
            }
            if (branch.getLogoUrl() != null) {
                merchantLogoImageView.setImage(new Image(branch.getLogoUrl()));
            }

            loadSideBar();
            enableMenu();
        });
        pause.play();


    }

    private void loadSideBar() {
        VBox rootVBox = App.appContextHolder.getRootContainer();

        List<Button> buttons = new ArrayList<>();
        for (String screen : App.appContextHolder.getEmployee().getScreenAccess()) {
            if (screen.equalsIgnoreCase("REGISTER")) {
                Button registerButton = (Button) rootVBox.getScene().lookup("#registerButton");
                buttons.add(registerButton);
            }
            if (screen.equalsIgnoreCase("MEMBER_PROFILE")) {
                Button memberInquiryButton = (Button) rootVBox.getScene().lookup("#memberInquiryButton");
                buttons.add(memberInquiryButton);
            }
            if (screen.equalsIgnoreCase("GIVE_POINTS")) {
                Button givePointsButton = (Button) rootVBox.getScene().lookup("#givePointsButton");
                buttons.add(givePointsButton);
            }
            if (screen.equalsIgnoreCase("GUEST_PURCHASE")) {
                Button guestPurchaseButton = (Button) rootVBox.getScene().lookup("#guestPurchaseButton");
                buttons.add(guestPurchaseButton);
            }
            if (screen.equalsIgnoreCase("OFFLINE_TRANSACTIONS")) {
                Button offlineButton = (Button) rootVBox.getScene().lookup("#offlineButton");
                buttons.add(offlineButton);
            }
            if (screen.equalsIgnoreCase("PAY_WITH_POINTS")) {
                Button payWithPointsButton = (Button) rootVBox.getScene().lookup("#payWithPointsButton");
                buttons.add(payWithPointsButton);
            }
            if (screen.equalsIgnoreCase("REDEEM_REWARDS")) {
                Button redeemRewardsButton = (Button) rootVBox.getScene().lookup("#redeemRewardsButton");
                buttons.add(redeemRewardsButton);
            }
            if (screen.equalsIgnoreCase("ISSUE_REWARDS")) {
                Button issueRewardsButton = (Button) rootVBox.getScene().lookup("#issueRewardsButton");
                buttons.add(issueRewardsButton);
            }

            if (screen.equalsIgnoreCase("TRANSACTIONS_VIEW")) {
                Button transactionsButton = (Button) rootVBox.getScene().lookup("#transactionsButton");
                buttons.add(transactionsButton);
            }

            if (screen.equalsIgnoreCase("OCR_SETTINGS")) {
                Button ocrButton = (Button) rootVBox.getScene().lookup("#ocrButton");
                buttons.add(ocrButton);
            }
            if (screen.equalsIgnoreCase("GIVE_STAMPS")) {
                Button giveStampsButton = (Button) rootVBox.getScene().lookup("#giveStampsButton");
                buttons.add(giveStampsButton);
            }
        }

        VBox sideBarVBox = (VBox) rootVBox.getScene().lookup("#sideBarVBox");
        sideBarVBox.getChildren().clear();
        for (Button button : buttons) {
            if (button != null) {
                button.setVisible(true);
                sideBarVBox.getChildren().add(button);
            }
        }
    }



}
