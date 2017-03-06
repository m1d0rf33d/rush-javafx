package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import com.yondu.service.CommonService;
import com.yondu.service.IssueRewardsService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

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

    private CommonService commonService = App.appContextHolder.commonService;
    private IssueRewardsService issueRewardsService = new IssueRewardsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        issueRewardsService.initialize();

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });


        rewardsFlowPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootContainer().setMinHeight(600 + newValue.doubleValue());
            }
        });



        App.appContextHolder.setCurrentState(AppState.ISSUE_REWARDS);
    }


}
