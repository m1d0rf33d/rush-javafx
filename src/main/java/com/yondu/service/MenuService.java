package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Employee;
import com.yondu.model.Merchant;
import com.yondu.model.constants.AppState;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
            Employee employee = App.appContextHolder.getEmployee();

            VBox rootVBox = App.appContextHolder.getRootContainer();
            MenuButton employeeMenuButton = (MenuButton) rootVBox.getScene().lookup("#employeeMenuButton");
            Label lbl1 = new Label("Hi! "+ employee.getEmployeeName());
            lbl1.getStyleClass().add("label-2");
            employeeMenuButton.setGraphic(lbl1);
            employeeMenuButton.getItems().clear();
            MenuItem m1 = new MenuItem();
            Label label = new Label("Logout");
            label.getStyleClass().add("label-2");
            label.setStyle("-fx-min-height: 20px;-fx-max-height: 20px;");
            m1.setGraphic(label);
            m1.getStyleClass().add("menu-item-hover");
            employeeMenuButton.getItems().add(m1);
            employeeMenuButton.getStyleClass().add("employee-combo-box");

            employeeMenuButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    label.setPrefWidth(employeeMenuButton.getWidth() - 43);
                    employeeMenuButton.getStyleClass().add("menu-hover");
                }
            });
            m1.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    App.appContextHolder.setEmployee(null);
                    App.appContextHolder.setCustomer(null);
                    App.appContextHolder.routeService.goToLoginScreen((Stage) rootVBox.getScene().getWindow());
                }
            });
            rootVBox.setOnMouseClicked((MouseEvent e) -> {
                employeeMenuButton.hide();
                employeeMenuButton.getStyleClass().remove("menu-hover");
            });

            Label branchNameLabel = (Label) rootVBox.getScene().lookup("#branchNameLabel");
            ImageView merchantLogoImageView = (ImageView) rootVBox.getScene().lookup("#merchantLogoImageView");
            branchNameLabel.setText(branch.getName());

            if (App.appContextHolder.getMerchant() != null && App.appContextHolder.getMerchant().getBackgroundUrl() != null) {
                Task task = imageLoaderWorker(App.appContextHolder.getMerchant().getBackgroundUrl());
                task.setOnSucceeded((Event e) -> {
                    if (App.appContextHolder.getCurrentState().equals(AppState.MENU)) {
                        Image image = (Image) task.getValue();
                        ImageView imageView = new ImageView(image);
                        VBox bodyStackPane = (VBox) rootVBox.getScene().lookup("#bodyStackPane");
                        bodyStackPane.getChildren().clear();
                        bodyStackPane.getChildren().add(imageView);
                    }

                });
                new Thread(task).start();

            }
            if (branch.getLogoUrl() != null) {

                Task task = imageLoaderWorker(branch.getLogoUrl());
                task.setOnSucceeded((Event e)-> {
                    Image image = (Image) task.getValue();
                    merchantLogoImageView.setImage(image);
                });
                new Thread(task).start();

            }

            loadSideBar();
            enableMenu();
        });
        pause.play();
    }

    private Task imageLoaderWorker(String url) {
        return new Task() {
            @Override
            protected Image call() throws Exception {
                return new Image(url);
            }
        };
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
