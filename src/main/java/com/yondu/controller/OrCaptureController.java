package com.yondu.controller;

import com.yondu.App;
import com.yondu.utils.ResizeHelper;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

/** JavaFx controller mapped to or-capture.fxml
 *
 *  @author m1d0rf33d
 */
public class OrCaptureController implements Initializable{

    @FXML
    public Pane capturePane;


    private static double xOffset = 0;
    private static double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        capturePane.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        capturePane.setOnMouseDragged((MouseEvent event) ->{
            Double stageX = App.appContextHolder.getOrCaptureStage().getX();
            Double stageY = App.appContextHolder.getOrCaptureStage().getY();

            Double width = App.appContextHolder.getOrCaptureStage().getWidth();
            Double height = App.appContextHolder.getOrCaptureStage().getHeight();
            if (event.getScreenX()  >  (stageX + 10) && event.getScreenY() > (stageY + 10) &&
                    event.getScreenX() < (stageX + width - 10) && event.getScreenY() < (stageY + height - 10)) {
                App.appContextHolder.getOrCaptureStage().setX(event.getScreenX() - xOffset);
                App.appContextHolder.getOrCaptureStage().setY(event.getScreenY() - yOffset);
            }
        });
    }
}
