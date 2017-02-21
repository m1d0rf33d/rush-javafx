package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/9/17.
 */
public class RedeemRewardsController implements Initializable {
    @FXML
    public Label nameLabel;
    @FXML
    public Label memberIdLabel;
    @FXML
    public Label mobileNumberLabel;

    @FXML
    public Label membershipDateLabel;
    @FXML
    public Label genderLabel;
    @FXML
    public Label birthdateLabel;

    @FXML
    public Label emailLabel;
    @FXML
    public Label pointsLabel;
    @FXML
    public FlowPane rewardsFlowPane;

    private Customer customer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        App.appContextHolder.setAppState(AppState.REDEEM_REWARDS);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        nameLabel.setText(customer.getName());
        memberIdLabel.setText(customer.getMemberId());
        mobileNumberLabel.setText(customer.getMobileNumber());
        membershipDateLabel.setText(customer.getMemberSince());
        genderLabel.setText(customer.getGender());
        birthdateLabel.setText(customer.getDateOfBirth());
        emailLabel.setText(customer.getEmail());
        pointsLabel.setText(customer.getAvailablePoints());
    }

    public void setRewards(List<Reward> rewards) {
        List<VBox> vBoxes = new ArrayList<>();
        for (Reward reward : rewards) {
            VBox vBox = new VBox();
            ImageView imageView = new ImageView(reward.getImageUrl());
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setOnMouseClicked((MouseEvent e) -> {
                showRewardsDialog(reward);
            });
            vBox.getChildren().add(imageView);
            Label label = new Label();
            label.setText(reward.getName());
            vBox.getChildren().addAll(label);
            vBox.setPrefWidth(200);
            vBox.setMargin(imageView, new Insets(10,10,10,10));
            vBox.setMargin(label, new Insets(10,10,10,10));
            vBox.setPrefHeight(200);
            vBoxes.add(vBox);
        }

        rewardsFlowPane.getChildren().addAll(vBoxes);
    }


    private void showRewardsDialog(Reward reward) {
        App.appContextHolder.getRootVBox().setOpacity(.50);
        for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
            n.setDisable(true);
        }

        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
            Parent root = fxmlLoader.load();
            RewardDialogController controller = fxmlLoader.getController();
            controller.setCustomer(customer);
            controller.setReward(reward);
            controller.setPointsLabel(pointsLabel);
            Scene scene = new Scene(root, 600,400);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    App.appContextHolder.getRootVBox().setOpacity(1);
                    for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                        n.setDisable(false);
                    }
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
