package com.yondu.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/7/17.
 */
public class RegisterController implements Initializable {

    @FXML
    public DatePicker birthdatePicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        birthdatePicker.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            birthdatePicker.show();
        });
    }
}
