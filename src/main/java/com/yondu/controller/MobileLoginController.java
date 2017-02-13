package com.yondu.controller;

import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/8/17.
 */
public class MobileLoginController implements Initializable {

    @FXML
    public Button cancelButton;
    @FXML
    public Button submitButton;
    @FXML
    public TextField mobileTextField;
    @FXML
    public Label errorLabel;

    private VBox rootVBox;
    private String targetScreen;
    private MenuService menuService = new MenuService();
    private RouteService routeService = new RouteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        errorLabel.setVisible(false);

        cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            rootVBox.setOpacity(1);
            for (Node n : rootVBox.getChildren()) {
                n.setDisable(false);
            }
            ((Stage) cancelButton.getScene().getWindow()).close();
        });

        submitButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            JSONObject jsonObject = menuService.loginCustomer(mobileTextField.getText());
            if (jsonObject.get("customer") != null) {
                if (targetScreen.equals(REDEEM_REWARDS_SCREEN)) {
                    ((Stage) submitButton.getScene().getWindow()).close();
                    routeService.loadRedeemRewardsScreen();
                } else if (targetScreen.equals(EARN_POINTS_SCREEN)) {
                    ((Stage) submitButton.getScene().getWindow()).close();
                    routeService.loadEarnPointsScreen();
                } else if (targetScreen.equals(PAY_WITH_POINTS)) {
                    ((Stage) submitButton.getScene().getWindow()).close();
                    routeService.loadPayWithPoints();
                } else if (targetScreen.equals(ISSUE_REWARDS_SCREEN)) {
                    ((Stage) submitButton.getScene().getWindow()).close();
                    routeService.loadIssueRewardsScreen();
                }

            } else {
                errorLabel.setVisible(true);
            }
        });

    }

    public VBox getRootVBox() {
        return rootVBox;
    }

    public void setRootVBox(VBox rootVBox) {
        this.rootVBox = rootVBox;
    }
    public String getTargetScreen() {
        return targetScreen;
    }

    public void setTargetScreen(String targetScreen) {
        this.targetScreen = targetScreen;
    }

}
