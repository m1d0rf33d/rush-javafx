package com.yondu.controller;

import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Created by aomine on 9/29/16.
 */
public class CaptureResultController {


    public Button okayButton;

    public void close() {
        ((Stage) this.okayButton.getScene().getWindow()).close();
    }
}
