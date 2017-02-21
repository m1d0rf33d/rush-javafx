package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.ApiService;
import com.yondu.service.IssueRewardsService;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.NotificationService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.CLAIM_REWARDS_ENDPOINT;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.PIN_SCREEN;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

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

    private Customer customer;
    private Reward reward;
    private Label pointsLabel;

    private IssueRewardsService issueRewardsService = new IssueRewardsService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        AppState state = App.appContextHolder.getAppState();
        if (state.equals(AppState.ISSUE_REWARDS)) {
            lockLabel.setVisible(false);
            redeemButton.setText("ISSUE REWARD");
        }

        redeemButton.setOnMouseClicked((MouseEvent e) -> {
            ((Stage) redeemButton.getScene().getWindow()).close();
            App.appContextHolder.getRootVBox().setOpacity(.50);
            for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(true);
            }
            if (state.equals(AppState.REDEEM_REWARDS)) {
                redeem();
            } else if (state.equals(AppState.ISSUE_REWARDS)) {
                issueReward();
            }
        });
    }

    private void issueReward() {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {

            ApiResponse apiResponse = issueRewardsService.issueReward(reward.getId());
            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }

            if (apiResponse.isSuccess()) {
                FlowPane rewardsFlowPane = (FlowPane) App.appContextHolder.getRootVBox().getScene().lookup("#rewardsFlowPane");
                rewardsFlowPane.getChildren().clear();

                ApiResponse apiResp = memberDetailsService.loginCustomer(App.appContextHolder.getCustomerMobile());
                if (apiResp.isSuccess()) {
                    List<VBox> vBoxes = new ArrayList<>();
                    for (Reward reward :((Customer) apiResp.getPayload().get("customer")).getActiveVouchers()) {

                        VBox vBox = new VBox();
                        ImageView imageView = new ImageView(reward.getImageUrl());
                        imageView.setFitWidth(200);
                        imageView.setFitHeight(200);
                        imageView.setOnMouseClicked((MouseEvent ee) -> {
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

            }
            Text text = new Text(apiResponse.getMessage());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
            alert.setHeaderText("ISSUE REWARD");
            alert.getDialogPane().setPadding(new Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();

        });
        pause.play();
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


    public void redeem() {
        try {
            ((Stage) redeemButton.getScene().getWindow()).close();
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            PinController controller = fxmlLoader.getController();
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

    public void setReward(Reward reward) {
        this.rewardImageView.setImage(new Image(reward.getImageUrl()));
        this.rewardImageView.setFitWidth(250);
        this.rewardImageView.setPreserveRatio(false);
        this.rewardImageView.setFitHeight(400);
        this.nameLabel.setText(reward.getName());
        this.detailsLabel.setText(reward.getDetails());
        this.reward = reward;

        if (App.appContextHolder.getAppState().equals(AppState.REDEEM_REWARDS)) {
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


    public void setCustomer(Customer customer) {
        this.customer = customer;

    }

    public Label getPointsLabel() {
        return pointsLabel;
    }

    public void setPointsLabel(Label pointsLabel) {
        this.pointsLabel = pointsLabel;
    }
}
