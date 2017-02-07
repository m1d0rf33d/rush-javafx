package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/7/17.
 */
public class MenuController implements Initializable {

    @FXML
    public StackPane bodyStackPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        try {
            bodyStackPane.getChildren().clear();
            Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/register.fxml"));
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
