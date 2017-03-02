package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.PointsRule;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.*;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.MEMBER_INQUIRY_SCREEN;

/**
 * Created by lynx on 2/9/17.
 */
public class EarnPointsController implements Initializable {
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

    private EarnPointsService earnPointsService = new EarnPointsService();
    private OcrService ocrService = new OcrService();
    private CommonService commonService = new CommonService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        earnPointsService.initialize();
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
                    pointsTextField.setText(df2.format(amount / pointsRule.getEarningPeso()));
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
