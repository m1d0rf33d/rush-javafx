package com.yondu.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/8/17.
 */
public class MobileLoginController implements Initializable {

    @FXML
    public Button cancelButton;

    private VBox rootVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            rootVBox.setOpacity(1);
            for (Node n : rootVBox.getChildren()) {
                n.setDisable(false);
            }
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
    }

    public VBox getRootVBox() {
        return rootVBox;
    }

    public void setRootVBox(VBox rootVBox) {
        this.rootVBox = rootVBox;
    }


}
