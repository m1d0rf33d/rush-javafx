package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Reward;
import com.yondu.model.constants.AppState;
import com.yondu.service.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/16/17.
 */
public class PinController implements Initializable {

    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField pinTextField;

    private PayWithPointsService payWithPointsService = App.appContextHolder.payWithPointsService;
    private RedeemRewardsService redeemRewardsService = App.appContextHolder.redeemRewardsService;
    private StampsService stampsService = App.appContextHolder.stampsService;
    private LoginService loginService = App.appContextHolder.loginService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        submitButton.setOnMouseClicked((MouseEvent e) -> {
            ((Stage) pinTextField.getScene().getWindow()).close();

            AppState state = App.appContextHolder.getCurrentState();
            if (state.equals(AppState.LOGIN)) {
                login();
            } else if (state.equals(AppState.PAY_WITH_POINTS)) {
                payWithPoints();
            } else if (state.equals(AppState.REDEEM_REWARDS)) {
                redeemRewards();
            } else if (state.equals(AppState.GIVE_STAMPS)) {
                redeemStamps();
            }

        });

        cancelButton.setOnMouseClicked((MouseEvent e) -> {
            VBox rootVBox = App.appContextHolder.getRootContainer();

            rootVBox.setOpacity(1);
            for (Node n : rootVBox.getChildren()) {
                n.setDisable(false);
            }
            ((Stage) pinTextField.getScene().getWindow()).close();
        });
    }

    private  void redeemStamps() {
        Reward reward = App.appContextHolder.getReward();
        String pin = pinTextField.getText();
        ApiResponse apiResponse = stampsService.redeemStamps(reward.getId(), pin);
        if (apiResponse.isSuccess()) {

        }

    }

    private void redeemRewards() {
        redeemRewardsService.redeemRewards(pinTextField.getText());
    }

    private void payWithPoints() {

       payWithPointsService.payWithPoints(pinTextField.getText());
    }

    private void login() {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField loginTextField = (TextField) rootVBox.getScene().lookup("#loginTextField");
        ComboBox branchComboBox = (ComboBox) rootVBox.getScene().lookup("#branchComboBox");

        String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();

        Branch selectedBranch = null;
        for (Branch branch : App.appContextHolder.getBranches()) {
            if (branch.getName().equals(branchName)) {
                selectedBranch = branch;
                break;
            }
        }

        loginService.loginEmployee(loginTextField.getText(), selectedBranch, pinTextField.getText());
    }

}
