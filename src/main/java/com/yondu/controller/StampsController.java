package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.*;
import com.yondu.service.CommonService;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.StampsService;
import com.yondu.utils.PropertyBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

/**
 * Created by erwin on 2/28/2017.
 */
public class StampsController  extends BaseController implements Initializable {
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
    @FXML
    public Button earnStampsButton;
    @FXML
    public TextField amountTextField;
    @FXML
    public Label availablePointsLabel;
    @FXML
    public Button exitButton;
    @FXML
    public ComboBox milestoneComboBox;
    @FXML
    public HBox amountHbox;
    @FXML
    public TextField orTextField;
    @FXML
    public HBox buttonHbox;
    @FXML
    public Button clearButton;
    @FXML
    public Label orLabel;

    private StampsService stampsService = App.appContextHolder.stampsService;
    private CommonService commonService = App.appContextHolder.commonService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        stampsService.initialize();


        rewardsFlowPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootContainer().setMinHeight(800 + newValue.doubleValue());
            }
        });

        earnStampsButton.setOnMouseClicked((MouseEvent e) -> {
            String amount = amountTextField.getText();
            String orNumber = orTextField.getText();


            String activity = (String) milestoneComboBox.getSelectionModel().getSelectedItem();
            if (activity.contains("Based")) {
                if (orNumber == null || orNumber != null && orNumber.isEmpty()) {
                    stampsService.showPrompt("OR Number is required", "Earn Stamps", false);
                    return;
                }
                stampsService.earnStamps(orNumber, amount);
            } else {
                String id = "";
                for (Milestone milestone : App.appContextHolder.getMerchant().getMilestones()) {
                    if (milestone.getName().equals(activity)) {
                        id = milestone.getId();
                    }
                }
                stampsService.earnMilestone(orNumber, id);
            }


        });

        Merchant merchant = App.appContextHolder.getMerchant();
        if (merchant.getMerchantType().equals("punchcard")) {
            pointsLabel.setVisible(false);
            availablePointsLabel.setVisible(false);
        }

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });
        milestoneComboBox.getItems().clear();
        milestoneComboBox.getItems().add("Based on purchase");
        for (Milestone milestone : merchant.getMilestones()) {
            milestoneComboBox.getItems().add(milestone.getName());
        }
        milestoneComboBox.getSelectionModel().selectFirst();

        milestoneComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                amountTextField.clear();
                orTextField.clear();
                if (!t1.contains("Based")) {
                   amountTextField.setDisable(true);
                    orLabel.setText("Remarks");
                } else {
                    amountTextField.setDisable(false);
                    orLabel.setText("OR Number");
                }
            }
        });
        clearButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                amountTextField.clear();
                orTextField.clear();
            }
        });
        PropertyBinder.bindNumberWitDot(amountTextField);
        PropertyBinder.bindVirtualKeyboard(amountTextField);
        PropertyBinder.bindVirtualKeyboard(orTextField);
    }

   /* @Override
    public void initialize(URL location, ResourceBundle resources) {

        rewardsFlowPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootContainer().setMinHeight(700 + newValue.doubleValue());
            }
        });

        earnStampsButton.setOnMouseClicked((MouseEvent e) -> {
            if (amountTextField.getText() == null || amountTextField.getText().isEmpty()) {
                return;
            }
            ApiResponse apiResponse = stampsService.earnStamps(amountTextField.getText().replace(",",""));
            if (apiResponse.isSuccess()) {
                CustomerCard  card = memberDetailsService.getCustomerCard();
                if (card != null && card.getPromo() != null) {
                 //   this.setRewards(card.getPromo().getRewards());
                }
            }
            commonService.showPrompt(apiResponse.getMessage(), "EARN STAMPS");
        });
    }

    public void loadCustomerDetails() {
        Customer customer = App.appContextHolder.getCustomer();
        nameLabel.setText(customer.getName());
        memberIdLabel.setText(customer.getMemberId());
        mobileNumberLabel.setText(customer.getMobileNumber());
        membershipDateLabel.setText(customer.getMemberSince());
        genderLabel.setText(customer.getGender());
        birthdateLabel.setText(customer.getDateOfBirth());
        emailLabel.setText(customer.getEmail());
        pointsLabel.setText(customer.getAvailablePoints());
    }
    public void loadStamps() {
        stampsService.loadStamps();
    }

    public void loadRewards() {

        rewardsFlowPane.getChildren().clear();
        Customer customer = App.appContextHolder.getCustomer();
        Promo promo = customer.getCard().getPromo();
        int stamps = promo.getStamps();
        List<VBox> vBoxes = new ArrayList<>();
        for (Reward reward : promo.getRewards()) {
            VBox vBox = new VBox();
            StackPane stackPane = new StackPane();
            ImageView imageView = new ImageView();
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);

            if (stamps < reward.getStamps()) {
                imageView.setImage(new Image(App.appContextHolder.getMerchant().getGrayStampsUrl()));
            } else {
                imageView.setImage(new Image(App.appContextHolder.getMerchant().getStampsUrl()));
                imageView.setOnMouseClicked((MouseEvent e) -> {
                    showRewardsDialog(reward);
                });
            }

            ImageView starImg = new ImageView();
            if (stamps >= reward.getStamps()) {
                starImg.setImage(new Image(App.class.getResource("/app/images/star-unredeemed.png").toExternalForm()));
            } else {

            }
            starImg.setFitHeight(50);
            starImg.setFitWidth(50);
            stackPane.setMargin(starImg, new Insets(70, 0,0,70));
            stackPane.getChildren().addAll(imageView, starImg);
            vBox.getChildren().add(stackPane);
            vBox.setPrefWidth(200);
            vBox.setMargin(stackPane, new Insets(10,10,10,10));
            vBox.setPrefHeight(200);
            vBoxes.add(vBox);
        }
        rewardsFlowPane.getChildren().addAll(vBoxes);
    }

    private void showRewardsDialog(Reward reward) {
        App.appContextHolder.getRootContainer().setOpacity(.50);
        for (Node n : App.appContextHolder.getRootContainer().getChildren()) {
            n.setDisable(true);
        }

        try {
            Stage stage = new Stage();
            stage.resizableProperty().setValue(Boolean.FALSE);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
            Parent root = fxmlLoader.load();
            RewardDialogController controller = fxmlLoader.getController();
            Scene scene = new Scene(root, 600,400);
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
    }*/

}
