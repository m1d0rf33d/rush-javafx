package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by erwin on 3/22/2017.
 */
public class LoadingController implements Initializable {

    @FXML
    public ImageView rushLogoImageView;

    @FXML
    public ImageView loadingImageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        loadingImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));
    }
}
