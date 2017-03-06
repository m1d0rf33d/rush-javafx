package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppState;
import com.yondu.service.CommonService;
import com.yondu.service.RedeemRewardsService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
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
    @FXML
    public javafx.scene.control.Button exitButton;

    private CommonService commonService = App.appContextHolder.commonService;
    private RedeemRewardsService redeemRewardsService = App.appContextHolder.redeemRewardsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        redeemRewardsService.initialize();

        exitButton.setOnMouseClicked((MouseEvent e) -> {
           commonService.exitMember();
        });


        rewardsFlowPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootContainer().setMinHeight(600 + newValue.doubleValue());
            }
        });
    }

}
