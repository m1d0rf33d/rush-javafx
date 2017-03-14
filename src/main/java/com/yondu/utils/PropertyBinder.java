package com.yondu.utils;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.model.Merchant;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.text.DecimalFormat;
import static com.yondu.model.constants.ApiConstants.*;
/**
 * Created by lynx on 2/21/17.
 */
public class PropertyBinder {

    public static void bindNumberOnly(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue !=null && (newValue != null && !newValue.isEmpty()) && !newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    public static void bindAmountOnly(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && (newValue != null && !newValue.isEmpty()) && !newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^,.\\d]", ""));
                }
            }
        });
    }

    public static void bindNumberWitDot(TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null && (newValue != null && !newValue.isEmpty()) && !newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^.\\d]", ""));
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
                    String removeComma = textField.getText().replaceAll(",", "");
                    textField.setText(decimalFormat.format(Double.parseDouble(removeComma)));
                }
            }
        });
    }

    public static void bindMaxLength(int length, TextField textField) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (textField.getText()!= null && textField.getText().length() > length) {
                    textField.setText(textField.getText().substring(0,length));
                }
            }
        });
    }

    public static void setNumberButtonClick(Button button, String number) {
        button.setOnMouseClicked((MouseEvent e) ->  {

            TextField loginTextField = (TextField) App.appContextHolder.getRootContainer().getScene().lookup("#loginTextField");
            String loginText = loginTextField.getText();
            if (loginText != null) {
                loginTextField.setText(loginText + number);
            } else {
                loginTextField.setText(number);
            }

        });
    }

    public static void bindVirtualKeyboard(TextField textField) {
        Merchant merchant = App.appContextHolder.getMerchant();
        if (merchant != null && merchant.getWithVk() != null && !merchant.getWithVk()) {
            textField.focusedProperty().addListener(new ChangeListener<Boolean>()
            {
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
                {
                    if (newPropertyValue)
                        FXVK.detach();
                }
            });
        }
    }
}
