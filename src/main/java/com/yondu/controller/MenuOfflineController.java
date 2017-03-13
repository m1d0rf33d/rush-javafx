package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by aomine on 3/12/17.
 */
public class MenuOfflineController implements Initializable {

    @FXML
    public ScrollPane rootScrollPane;
    @FXML
    public VBox rootVBox;
    @FXML
    public VBox sideBarVBox;
    @FXML
    public MenuButton employeeMenuButton;
    @FXML
    public Button menuEarnButton;
    @FXML
    public VBox bodyStackPane;
    @FXML
    public Button ocrButton;
    @FXML
    public Button transactionsButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        App.appContextHolder.setRootContainer(rootVBox);

        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);
        rootScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        employeeMenuButton.setMinWidth(140);
        employeeMenuButton.setText("OFFLINE");
        employeeMenuButton.getItems().clear();

        Label label = new Label("GO ONLINE");
        label.setId("logoutLabel");
        MenuItem logoutMenuItem = new MenuItem();
        logoutMenuItem.setGraphic(label);
        logoutMenuItem.getGraphic().setStyle("-fx-min-width: " + employeeMenuButton.getWidth() + "px;");
        logoutMenuItem.getStyleClass().add("menuitem");
        logoutMenuItem.setId("logoutButton");
        logoutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                App.appContextHolder.loginService.reconnectSuccess();
            }
        });
        employeeMenuButton.getItems().addAll(logoutMenuItem);

        menuEarnButton.setOnMouseClicked((Event e) -> {
            App.appContextHolder.setCurrentState(AppState.EARN_OFFLINE);
            App.appContextHolder.commonService.updateButtonState();
            try {
                bodyStackPane.getChildren().clear();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/login-offline.fxml"));
                Parent root = fxmlLoader.load();
                bodyStackPane.getChildren().add(root);

            } catch (IOException err) {
                err.printStackTrace();
            }

        });

        ocrButton.setOnMouseClicked((Event e) -> {
            App.appContextHolder.setCurrentState(AppState.OCR);
            App.appContextHolder.commonService.updateButtonState();
            try {
                bodyStackPane.getChildren().clear();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/ocr.fxml"));
                Parent root = fxmlLoader.load();
                bodyStackPane.getChildren().add(root);

            } catch (IOException err) {
                err.printStackTrace();
            }

        });

        transactionsButton.setOnMouseClicked((Event e) -> {
            App.appContextHolder.setCurrentState(AppState.TRANSACTIONS);
            App.appContextHolder.commonService.updateButtonState();
            try {
                bodyStackPane.getChildren().clear();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/branch-transactions.fxml"));
                Parent root = fxmlLoader.load();
                bodyStackPane.getChildren().add(root);

            } catch (IOException err) {
                err.printStackTrace();
            }

        });
    }

}
