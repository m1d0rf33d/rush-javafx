package com.yondu.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/** Javafx controller mapped to capture-result.fxml
 *
 *  @author m1d0rf33d
 */
public class CaptureResultController {

    @FXML
    public Button okayButton;

    public void close() {
        ((Stage) this.okayButton.getScene().getWindow()).close();
    }
}
