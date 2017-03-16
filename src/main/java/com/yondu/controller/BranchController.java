package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.PropertyBinder;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by aomine on 3/12/17.
 */
public class BranchController implements Initializable {

    @FXML
    public TextField searchTextField;
    @FXML
    public Tab offlineTab;
    @FXML
    public Tab onlineTab;
    @FXML
    public TabPane transactionsTabPane;
    @FXML
    public Button givePointsButton;
    @FXML
    public Button generateButton;
    @FXML
    public Button onlineGenerateButton;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        App.appContextHolder.branchTransactionService.initialize();
        offlineTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                for (Tab tab : transactionsTabPane.getTabs()) {
                    if (tab.getId().equals("offlineTab")) {
                        tab.setGraphic(new Label("OFFLINE"));
                    } else {
                        tab.setGraphic(new Label("ONLINE"));
                    }
                    tab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                            "    -fx-tab-max-width:200px;\n" +
                            "    -fx-tab-min-height:30px;\n" +
                            "    -fx-tab-max-height:30px;\n" +
                            "    -fx-text-fill: white;\n" +
                            "    -fx-font-size: 17px;");
                }

                offlineTab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                        "    -fx-tab-max-width:200px;\n" +
                        "    -fx-tab-min-height:30px;\n" +
                        "    -fx-tab-max-height:30px;\n" +
                        "    -fx-text-fill: black;\n" +
                        "    -fx-font-size: 17px;");
            }
        });

        onlineTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                for (Tab tab : transactionsTabPane.getTabs()) {
                    if (tab.getId().equals("offlineTab")) {
                        tab.setGraphic(new Label("OFFLINE"));
                    } else {
                        tab.setGraphic(new Label("ONLINE"));
                    }
                    tab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                            "    -fx-tab-max-width:200px;\n" +
                            "    -fx-tab-min-height:30px;\n" +
                            "    -fx-tab-max-height:30px;\n" +
                            "    -fx-text-fill: white;\n" +
                            "    -fx-font-size: 17px;");
                }
                onlineTab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                        "    -fx-tab-max-width:200px;\n" +
                        "    -fx-tab-min-height:30px;\n" +
                        "    -fx-tab-max-height:30px;\n" +
                        "    -fx-text-fill: black;\n" +
                        "    -fx-font-size: 17px;");
            }
        });

        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.offlineService.givePoints();
        });

        generateButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.branchTransactionService.exportOfflineReport();
        });

        onlineGenerateButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.branchTransactionService.exportOnlineReport();
        });
    }


}
