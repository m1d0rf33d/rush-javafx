package com.yondu.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppState;
import com.yondu.service.ApiService;
import com.yondu.service.CommonService;
import com.yondu.service.RouteService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
import static com.yondu.AppContextHolder.UNCLAIMED_REWARDS_ENDPOINT;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.MEMBER_INQUIRY_SCREEN;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

/**
 * Created by lynx on 2/10/17.
 */
public class IssueRewardsController implements Initializable{

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
    public VBox issueRootVBox;
    @FXML
    public Button exitButton;

    private Customer customer;
    private ApiService apiService = new ApiService();

    private List<Reward> unclaimedRewards = new ArrayList<>();
    private CommonService commonService = new CommonService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });


        rewardsFlowPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootVBox().setMinHeight(600 + newValue.doubleValue());
            }
        });

        App.appContextHolder.setAppState(AppState.ISSUE_REWARDS);
        getUnclaimedRewards();

    }
    private void getUnclaimedRewards() {
        String url = BASE_URL + UNCLAIMED_REWARDS_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        url = url.replace(":customer_id", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        String x = "";
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                for (JSONObject data : dataJSON) {
                    Reward reward = new Reward();
                    reward.setId((String) data.get("id"));
                    JSONObject innerJSON = (JSONObject) data.get("reward");
                    reward.setName((String) innerJSON.get("name"));
                    unclaimedRewards.add(reward);
                }
            }
        }
    }

    public void setRewards(List<Reward> rewards) {
        List<ImageView> imageViews = new ArrayList<>();
        List<VBox> vBoxes = new ArrayList<>();
        for (Reward reward : rewards) {
            for (Reward rew : unclaimedRewards) {
                if (rew.getName().equals(reward.getName())) {
                    reward.setId(rew.getId());
                }
            }
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
            label.getStyleClass().add("lbl-med");
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
            stage.resizableProperty().setValue(Boolean.FALSE);
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
}
