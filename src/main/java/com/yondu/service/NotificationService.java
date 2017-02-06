package com.yondu.service;

import com.yondu.model.constants.AppConfigConstants;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Created by lynx on 2/2/17.
 */
public class NotificationService {

    public void showMessagePrompt(String message,
                          Alert.AlertType alertType,
                          Window window,
                          ButtonType ... buttonTypes) {

        Text text = new Text(message);

        Alert alert = new Alert(alertType, message, buttonTypes);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(window);
        alert.getDialogPane().setContent(text);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }
    }
}
