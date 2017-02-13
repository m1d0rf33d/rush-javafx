package com.yondu.controller;

import com.yondu.model.Reward;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/10/17.
 */
public class RewardDialogController implements Initializable {


    @FXML
    public ImageView rewardImageView;
    @FXML
    public Label nameLabel;
    @FXML
    public Label detailsLabel;
    @FXML
    public Button redeemButton;
    @FXML
    public Label lockLabel;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setReward(Reward reward) {
        this.rewardImageView.setImage(new Image(reward.getImageUrl()));
        this.rewardImageView.setFitWidth(250);
        this.rewardImageView.setPreserveRatio(false);
        this.rewardImageView.setFitHeight(400);
        this.nameLabel.setText(reward.getName());
        this.detailsLabel.setText(reward.getDetails());
     }

}
