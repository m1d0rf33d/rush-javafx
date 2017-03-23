package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by aomine on 3/23/17.
 */
public class NotifController implements Initializable{

    @FXML
    public ImageView iconImageView;

    @FXML
    public Label headerLabel;
    @FXML
    public Label messageLabel;

    @FXML
    public Button okayButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okayButton.setOnMouseClicked((MouseEvent e) -> {
            ((Stage) okayButton.getScene().getWindow()).close();
            VBox rootVBox = App.appContextHolder.getRootContainer();
            rootVBox.setOpacity(1);
            for (Node n :  rootVBox.getChildren()) {
                n.setDisable(false);
            }
        });
    }

    public void initAfterLoad(String header, String message, Boolean success) {
        if (success) {
            iconImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/success-icon.png").toExternalForm()));
        } else {
            iconImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/error-icon.png").toExternalForm()));
        }
        headerLabel.setText(header);
        messageLabel.setText(message);

    }
}
