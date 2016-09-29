package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by aomine on 9/29/16.
 */
public class OrCaptureController {

    private static final String CAPTURE_RESULT_FXML = "/app/fxml/capture-result.fxml";
    @FXML
    public Button captureButton;

    public void captureOrDimension() {

        Stage stage = (Stage) this.captureButton.getScene().getWindow();
        Double posX = stage.getX(),
                posY = stage.getY(),
                width = stage.getWidth(),
                height = stage.getHeight();

        App.appContextHolder.setOrNumberPosX(posX.intValue());
        App.appContextHolder.setOrNumberPosY(posY.intValue());
        App.appContextHolder.setOrNumberWidth(width.intValue());
        App.appContextHolder.setOrNumberHeight(height.intValue());

        Stage resultStage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(App.class.getResource(CAPTURE_RESULT_FXML));
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultStage.setScene(new Scene(root, 400,150));
        resultStage.setX(400);
        resultStage.setY(400);
        resultStage.resizableProperty().setValue(false);
        resultStage.show();

        ((Stage) this.captureButton.getScene().getWindow()).close();
    }
}
