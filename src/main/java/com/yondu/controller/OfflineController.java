package com.yondu.controller;

import com.yondu.App;
import com.yondu.service.OfflineService;
import com.yondu.utils.PropertyBinder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/10/17.
 */
public class OfflineController implements Initializable {

    @FXML
    public MenuButton statusMenuButton;
    @FXML
    public Pagination offlinePagination;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button givePointsButton;

    private OfflineService offlineService = App.appContextHolder.offlineService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertyBinder.bindVirtualKeyboard(searchTextField);
        offlineService.initialize();
        buildStatusFilters();

        statusMenuButton.setText("All");

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            offlineService.givePoints();
        });

    }


    private void buildStatusFilters() {
        Label allLabel = new Label("ALL");
        allLabel.setPrefWidth(150);
        allLabel.setWrapText(true);
        MenuItem allItem = new MenuItem();
        allItem.setGraphic(allLabel);
        allItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                statusMenuButton.setText("ALL");
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        Label pendingLabel = new Label("PENDING");
        pendingLabel.setPrefWidth(170);
        pendingLabel.setWrapText(true);
        MenuItem pendingItem = new MenuItem();
        pendingItem.setGraphic(pendingLabel);
        pendingItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("PENDING");
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });


        Label submittedLabel = new Label("SUBMITTED");
        submittedLabel.setPrefWidth(150);
        submittedLabel.setWrapText(true);
        MenuItem submittedItem = new MenuItem();
        submittedItem.setGraphic(submittedLabel);
        submittedItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("SUBMITTED");
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        Label failedLabel = new Label("FAILED");
        failedLabel.setPrefWidth(150);
        failedLabel.setWrapText(true);
        MenuItem failedItem = new MenuItem();
        failedItem.setGraphic(failedLabel);
        failedItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("FAILED");
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        statusMenuButton.getItems().clear();
        statusMenuButton.getItems().addAll(allItem, pendingItem, submittedItem, failedItem);
    }

}
