package com.yondu.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.text.DecimalFormat;

/**
 * Created by lynx on 2/21/17.
 */
public class PropertyBinder {

    public static void bindNumberOnly(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    public static void bindAmountOnly(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^,.\\d]", ""));
                }
            }
        });
    }

    public static void addComma(TextField textField) {
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!textField.getText().isEmpty()) {
                    DecimalFormat decimalFormat = new DecimalFormat("###,###,###.##");
                    textField.setText(decimalFormat.format(Double.parseDouble(textField.getText())));
                }
            }
        });
    }

    public static void bindMaxLength(int length, TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (textField.getText().length() > length) {
                    textField.setText(textField.getText().substring(0,length));
                }
            }
        });
    }

}
