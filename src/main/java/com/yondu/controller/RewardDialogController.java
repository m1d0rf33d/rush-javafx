package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Merchant;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppState;
import com.yondu.service.IssueRewardsService;
import com.yondu.service.MemberDetailsService;
import com.yondu.utils.PropertyBinder;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.poi.ss.formula.functions.Even;

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
    @FXML
    public TextField quantityTextField;
    @FXML
    public HBox quantityHBox;
    @FXML
    public Button minusButton;
    @FXML
    public Button plusButton;

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
                Reward reward = App.appContextHolder.getReward();
                reward.setQuantity(quantityTextField.getText());
                showPinDialog();
            } else if (state.equals(AppState.ISSUE_REWARDS)) {
                Merchant merchant = App.appContextHolder.getMerchant();
                if (merchant.getMerchantType().equals("loyalty")) {
                    issueReward();
                } else {
                    showPinDialog();
                }

            } else if (state.equals(AppState.GIVE_STAMPS)) {
                showPinDialog();
            }
        });
    }

    private void issueReward() {
        Reward reward = App.appContextHolder.getReward();
        ((Stage) rewardImageView.getScene().getWindow()).close();
        issueRewardsService.issueReward(reward.getRedeemId());
    }

    public void showPinDialog() {
        try {
            ((Stage) redeemButton.getScene().getWindow()).close();
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 420,220);
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

        Task task = imageLoadWorker(reward.getImageUrl());
        task.setOnSucceeded((Event e) -> {
            Image image = (Image) task.getValue();
            this.rewardImageView.setImage(image);
        });
        new Thread(task).start();

        this.rewardImageView.setFitWidth(350);
        this.rewardImageView.setPreserveRatio(false);
        this.rewardImageView.setFitHeight(200);
        this.nameLabel.setText(reward.getName());
        this.detailsLabel.setText(reward.getDetails());

        Merchant merchant = App.appContextHolder.getMerchant();
        if (merchant.getMerchantType().equals("punchcard")) {
            quantityHBox.setVisible(false);

            if (App.appContextHolder.getCurrentState().equals(AppState.GIVE_STAMPS)) {
                if (reward.getDate() != null) {
                    redeemButton.setVisible(false);
                    lockLabel.setText("Redemption date: " + reward.getDate());
                } else {
                    lockLabel.setVisible(false);
                }

                this.requiredPointsLabel.setText(reward.getStamps() + " stamps");
            } else if (App.appContextHolder.getCurrentState().equals(AppState.ISSUE_REWARDS)) {
                lockLabel.setVisible(false);
            }
            this.requiredPointsLabel.setVisible(false);

        } else {

            if (App.appContextHolder.getCurrentState().equals(AppState.REDEEM_REWARDS)) {
                Customer customer = App.appContextHolder.getCustomer();
                quantityTextField.setText("1");
                PropertyBinder.bindNumberOnly(quantityTextField);
                PropertyBinder.bindDefaultOne(quantityTextField);
                plusButton.setOnMouseClicked((Event e)-> {
                    Integer q = Integer.parseInt(quantityTextField.getText());
                    quantityTextField.setText(String.valueOf(++q));
                });

                minusButton.setOnMouseClicked((Event e)-> {
                    Integer q = Integer.parseInt(quantityTextField.getText());
                    quantityTextField.setText(String.valueOf(--q));
                });

                Long pointsRequired = Long.parseLong(reward.getPointsRequired());
                Double customerPoints = Double.parseDouble(customer.getAvailablePoints());
                if (pointsRequired > customerPoints) {
                    lockLabel.setVisible(true);
                    redeemButton.setVisible(false);
                } else {
                    lockLabel.setVisible(false);
                    redeemButton.setVisible(true);
                }
            } else {
                quantityHBox.setVisible(false);
            }
            this.requiredPointsLabel.setText(reward.getPointsRequired() + " points");
        }
    }

    private Task imageLoadWorker(String url) {
        return new Task() {
            @Override
            protected Image call() throws Exception {
                return new Image(url);
            }
        };
    }
}
