package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppState;
import com.yondu.service.IssueRewardsService;
import com.yondu.service.MemberDetailsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.PIN_SCREEN;

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
    @FXML
    public Label requiredPointsLabel;

    private IssueRewardsService issueRewardsService = App.appContextHolder.issueRewardsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        renderReward();

        AppState state = App.appContextHolder.getCurrentState();
        if (state.equals(AppState.ISSUE_REWARDS)) {
            lockLabel.setVisible(false);
            redeemButton.setText("ISSUE REWARD");
        }

        redeemButton.setOnMouseClicked((MouseEvent e) -> {

            if (state.equals(AppState.REDEEM_REWARDS)) {
                showPinDialog();
            } else if (state.equals(AppState.ISSUE_REWARDS)) {
                issueReward();
            } else if (state.equals(AppState.GIVE_STAMPS)) {
                showPinDialog();
            }
        });
    }

    private void issueReward() {
        Reward reward = App.appContextHolder.getReward();
        ((Stage) rewardImageView.getScene().getWindow()).close();
        issueRewardsService.issueReward(reward.getId());
    }

    public void showPinDialog() {
        try {
            ((Stage) redeemButton.getScene().getWindow()).close();
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 500,300);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    App.appContextHolder.getRootContainer().setOpacity(1);
                    for (Node n : App.appContextHolder.getRootContainer().getChildren()) {
                        n.setDisable(false);
                    }

                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderReward() {
        Reward reward = App.appContextHolder.getReward();

        this.rewardImageView.setImage(new Image(reward.getImageUrl()));
        this.rewardImageView.setFitWidth(350);
        this.rewardImageView.setPreserveRatio(false);
        this.rewardImageView.setFitHeight(200);
        this.nameLabel.setText(reward.getName());
        this.detailsLabel.setText(reward.getDetails());
        this.requiredPointsLabel.setText(reward.getPointsRequired() + " points");

        if (App.appContextHolder.getCurrentState().equals(AppState.REDEEM_REWARDS)) {
            Customer customer = App.appContextHolder.getCustomer();

            Long pointsRequired = Long.parseLong(reward.getPointsRequired());
            Double customerPoints = Double.parseDouble(customer.getAvailablePoints());
            if (pointsRequired > customerPoints) {
                lockLabel.setVisible(true);
                redeemButton.setVisible(false);
            } else {
                lockLabel.setVisible(false);
                redeemButton.setVisible(true);
            }
        }
    }
}
