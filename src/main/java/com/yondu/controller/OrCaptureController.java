package com.yondu.controller;

import com.yondu.App;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/** JavaFx controller mapped to or-capture.fxml
 *
 *  @author m1d0rf33d
 */
public class OrCaptureController implements Initializable{


    @FXML
    public Button captureButton;
    @FXML
    public Pane capturePane;


    private static double xOffset = 0;
    private static double yOffset = 0;


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

        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Target screen area captured.", ButtonType.OK);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }

        ((Stage) this.captureButton.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        capturePane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                xOffset = ((Stage) captureButton.getScene().getWindow()).getX() - event.getScreenX();
                yOffset = ((Stage) captureButton.getScene().getWindow()).getY() - event.getScreenY();
            }
        });
        capturePane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ((Stage) captureButton.getScene().getWindow()).setX(event.getScreenX() + xOffset);
                ((Stage) captureButton.getScene().getWindow()).setY(event.getScreenY() + yOffset);
            }
        });
        captureButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                captureOrDimension();
            }
        });
    }
}
