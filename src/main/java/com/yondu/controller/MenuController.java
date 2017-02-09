package com.yondu.controller;

import com.yondu.App;
import com.yondu.AppContextHolder;
import com.yondu.model.Customer;
import com.yondu.service.MenuService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;

import java.beans.EventHandler;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/7/17.
 */
public class MenuController implements Initializable {

    @FXML
    public StackPane bodyStackPane;
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

    private MenuService menuService = new MenuService();


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        App.appContextHolder.setRootVBox(rootVBox);
        App.appContextHolder.setRootStackPane(bodyStackPane);

        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            loadContentPage(REGISTER_SCREEN);
        });
        memberInquiryButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadContentPage(MEMBER_INQUIRY_SCREEN);
            } else {
                loadContentPage(MEMBER_DETAILS_SCREEN);
            }
        });

        transactionsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            if (App.appContextHolder.getCustomerMobile() == null) {
                loadMobileLoginDialog();
            } else {
                loadTransactionsPage();
            }
        });
    }

    private void loadTransactionsPage() {
        try {
            JSONObject jsonObject = menuService.loginCustomer(App.appContextHolder.getCustomerMobile());
            if (jsonObject != null) {
                bodyStackPane.getChildren().clear();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(TRANSACTIONS_SCREEN));
                Parent root =  fxmlLoader.load();
                TransactionsController controller = fxmlLoader.getController();
                controller.setCustomer((Customer) jsonObject.get("customer"));
                bodyStackPane.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadContentPage(String page) {
        try {
            bodyStackPane.getChildren().clear();
            Parent root = FXMLLoader.load(App.class.getResource(page));
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMobileLoginDialog() {
        try {
            disableMenu();

            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MOBILE_LOGIN_SCREEN));
            Parent root = fxmlLoader.load();
            MobileLoginController controller = fxmlLoader.getController();
            controller.setRootVBox(rootVBox);
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

}
