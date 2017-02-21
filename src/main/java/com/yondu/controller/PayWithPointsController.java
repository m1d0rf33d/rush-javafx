package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.PointsRule;
import com.yondu.model.constants.AppState;
import com.yondu.service.EarnPointsService;
import com.yondu.service.MenuService;
import com.yondu.service.RouteService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.PIN_SCREEN;

/**
 * Created by lynx on 2/10/17.
 */
public class PayWithPointsController implements Initializable {
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
    public Label pesoValueLabel;
    @FXML
    public TextField pointsTextField;
    @FXML
    public TextField receiptTextField;
    @FXML
    public TextField amountTextField;
    @FXML
    public Button clearButton;
    @FXML
    public Button submitButton;

    private Customer customer;
    private RouteService routeService = new RouteService();
    private MenuService menuService = new MenuService();
    private EarnPointsService earnPointsService = new EarnPointsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        App.appContextHolder.setAppState(AppState.PAY_WITH_POINTS);


        amountTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    amountTextField.setText(newValue.replaceAll("[^,.\\d]", ""));
                }
            }
        });

        amountTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!amountTextField.getText().isEmpty()) {
                    DecimalFormat decimalFormat = new DecimalFormat("###,###,###.##");
                    amountTextField.setText(decimalFormat.format(Double.parseDouble(amountTextField.getText())));
                }
            }
        });

        pointsTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    amountTextField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        submitButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.getRootVBox().setOpacity(.50);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(true);
            }


            routeService.loadPinScreen(receiptTextField, amountTextField, pointsTextField, pointsLabel, pesoValueLabel);
        });

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            amountTextField.setText(null);
            receiptTextField.setText(null);
            pointsTextField.setText(null);
        });
    }


    public Customer getCustomer() {
        return customer;
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

        ApiResponse apiResponse = earnPointsService.getPointsRule();
        if (apiResponse.isSuccess()) {
            PointsRule pointsRule = (PointsRule) apiResponse.getPayload().get("pointsRule");
            pesoValueLabel.setText(String.valueOf(Double.parseDouble(customer.getAvailablePoints()) * pointsRule.getRedeemPeso()));
        }

    }
}
