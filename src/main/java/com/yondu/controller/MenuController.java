package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;

/**
 * Created by lynx on 2/7/17.
 */
public class MenuController implements Initializable {

    @FXML
    public StackPane bodyStackPane;
    @FXML
    public Button registerButton;
    @FXML
    public Button memberInquiryButton;
    @FXML
    public Button givePointsButton;
    @FXML
    public VBox rootVBox;

    private String REGISTER_SCREEN = "/app/fxml/register.fxml";
    private String MEMBER_INQUIRY_SCREEN = "/app/fxml/member-inquiry.fxml";


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            loadContentPage(REGISTER_SCREEN);
        });
        memberInquiryButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            loadContentPage(MEMBER_INQUIRY_SCREEN);
        });

        givePointsButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            rootVBox.setOpacity(.50);
            loadMobileLoginDialog();
        });

    }



    private void loadContentPage(String page) {
        try {
            bodyStackPane.getChildren().clear();
            Parent root = FXMLLoader.load(App.class.getResource(page));
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMobileLoginDialog() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/mobile-login.fxml"));
            stage.setScene(new Scene(root, 600, 400));
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(rootVBox.getScene().getWindow());
            stage.setAlwaysOnTop(true);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
