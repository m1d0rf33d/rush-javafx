package com.yondu.service;

import com.yondu.model.constants.AppConfigConstants;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.layout.VBox;
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
                          VBox rootVBox,
                          ButtonType ... buttonTypes) {
        if (rootVBox != null) {
            rootVBox.setOpacity(.50);
            for (Node n : rootVBox.getChildren()) {
                n.setDisable(true);
            }
        }

        Text text = new Text(message);

        Alert alert = new Alert(alertType, message, buttonTypes);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(window);
        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.setOnCloseRequest((DialogEvent e) -> {
            if (rootVBox != null) {
                rootVBox.setOpacity(1);
                for (Node n : rootVBox.getChildren()) {
                    n.setDisable(false);
                }
            }
        });
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            alert.close();
            if (rootVBox != null) {
                rootVBox.setOpacity(1);
                for (Node n : rootVBox.getChildren()) {
                    n.setDisable(false);
                }
            }
        }
    }
}
