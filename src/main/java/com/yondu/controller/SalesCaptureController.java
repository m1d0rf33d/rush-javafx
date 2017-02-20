package com.yondu.controller;

import com.yondu.App;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/** JavaFx controller mapped to sales-capture.fxml
 *
 *  @m1d0rf33d
 */
public class SalesCaptureController implements Initializable {

    @FXML
    public Pane salesPane;
    @FXML
    public Label orLabel;

    private static double xOffset = 0;
    private static double yOffset = 0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        salesPane.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        salesPane.setOnMouseDragged((MouseEvent event) ->{
            Double stageX = App.appContextHolder.getSalesCaptureStage().getX();
            Double stageY = App.appContextHolder.getSalesCaptureStage().getY();
            Double width = App.appContextHolder.getSalesCaptureStage().getWidth();
            Double height = App.appContextHolder.getSalesCaptureStage().getHeight();
            if (event.getScreenX()  >  (stageX + 10) && event.getScreenY() > (stageY + 10) &&
                    event.getScreenX() < (stageX + width - 10) && event.getScreenY() < (stageY + height - 10)) {
                App.appContextHolder.getSalesCaptureStage().setX(event.getScreenX() - xOffset);
                App.appContextHolder.getSalesCaptureStage().setY(event.getScreenY() - yOffset);
            }
        });
    }
}
