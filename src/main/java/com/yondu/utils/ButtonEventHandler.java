package com.yondu.utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Created by erwin on 10/11/2016.
 */
public class ButtonEventHandler implements EventHandler<ActionEvent> {

    private TextField loginTextField;

    public ButtonEventHandler(TextField loginTextField) {
        this.loginTextField = loginTextField;
    }

    @Override
    public void handle(ActionEvent event) {

        Button btn = (Button) event.getSource();
        if (btn.getText().equalsIgnoreCase("X")) {
            loginTextField.setText("");
        } else {
            loginTextField.setText(loginTextField.getText() + btn.getText());
        }

    }
}
