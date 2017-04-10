package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.PointsRule;
import com.yondu.service.CommonService;
import com.yondu.service.EarnPointsService;
import com.yondu.service.OcrService;
import com.yondu.utils.PropertyBinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/9/17.
 */
public class EarnPointsController extends BaseController implements Initializable {
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
    public TextField receiptTextField;
    @FXML
    public TextField amountTextField;
    @FXML
    public TextField pointsTextField;
    @FXML
    public Button submitButton;
    @FXML
    public Button clearButton;
    @FXML
    public Button ocrButton;
    @FXML
    public Button exitButton;

    private DecimalFormat df2 = new DecimalFormat(".##");

    private EarnPointsService earnPointsService = App.appContextHolder.earnPointsService;
    private OcrService ocrService = App.appContextHolder.ocrService;
    private CommonService commonService = App.appContextHolder.commonService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        PropertyBinder.bindVirtualKeyboard(amountTextField);
        PropertyBinder.bindVirtualKeyboard(receiptTextField);

        earnPointsService.initialize();

        if (!readOcrConfig()) {
            ocrButton.setVisible(false);
        }


        pointsTextField.setDisable(true);

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });


        submitButton.setOnMouseClicked((MouseEvent e) -> {
            earnPoints();
        });

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            pointsTextField.setText(null);
            amountTextField.setText(null);
            receiptTextField.setText(null);
        });

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (amountTextField != null && !amountTextField.getText().isEmpty()) {
                Double amount = Double.parseDouble(amountTextField.getText().replaceAll("[,]", ""));
                PointsRule pointsRule = App.appContextHolder.getPointsRule();
                if (pointsRule != null) {
                    Long amt = amount.longValue();
                    pointsTextField.setText(String.valueOf(amt / pointsRule.getEarningPeso()));
                }
            } else {
                pointsTextField.setText(null);
            }
        });
        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            ocrService.triggerOCR();
        });

        PropertyBinder.bindAmountOnly(amountTextField);
        PropertyBinder.addComma(amountTextField);
    }
    private void earnPoints() {
        String orNumber = receiptTextField.getText();
        String amount = amountTextField.getText().replace(",", "");
        earnPointsService.earnPoints(orNumber, amount);
    }


}
