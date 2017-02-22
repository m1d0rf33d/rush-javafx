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
import java.util.ResourceBundle;

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

    private Customer customer;
    private PointsRule pointsRule;

    private EarnPointsService earnPointsService = new EarnPointsService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();
    private OcrService ocrService = new OcrService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pointsTextField.setDisable(true);
        ApiResponse apiResponse = earnPointsService.getPointsRule();
        if (apiResponse.isSuccess()) {
            pointsRule = (PointsRule) apiResponse.getPayload().get("pointsRule");
        }


        submitButton.setOnMouseClicked((MouseEvent e) -> {
            earnPoints();
        });

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            pointsTextField.setText(null);
            amountTextField.setText(null);
            receiptTextField.setText(null);
        });

        amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!amountTextField.getText().isEmpty()) {
                Double amount = Double.parseDouble(amountTextField.getText().replaceAll("[.,]", ""));
                if (pointsRule != null) {
                    pointsTextField.setText(String.valueOf(amount / pointsRule.getEarningPeso()));
                }
            } else {
                pointsTextField.setText(null);
            }
        });


        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.getRootVBox().setOpacity(.50);
            for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(true);
            }

            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.5)
            );
            pause.setOnFinished(event -> {

                ((Stage) App.appContextHolder.getRootVBox().getScene().getWindow()).setIconified(true);
                PauseTransition p = new PauseTransition(
                        Duration.seconds(.50)
                );
                p.setOnFinished(ev -> {
                    ApiResponse apiResp = ocrService.triggerOCR();
                    if (apiResp.isSuccess()) {
                        String receiptNo = (String) apiResp.getPayload().get("orNumber");
                        String amount = (String) apiResp.getPayload().get("amount");
                        receiptTextField.setText(receiptNo);
                        amountTextField.setText(amount);
                    } else {
                        Text text = new Text(apiResp.getMessage());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                        alert.setTitle(AppConfigConstants.APP_TITLE);
                        alert.initStyle(StageStyle.UTILITY);
                        alert.initOwner(receiptTextField.getScene().getWindow());
                        alert.setHeaderText("OCR CAPTURE");
                        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
                        alert.getDialogPane().setContent(text);
                        alert.getDialogPane().setPrefWidth(400);
                        alert.show();
                    }
                    ((Stage) App.appContextHolder.getRootVBox().getScene().getWindow()).setIconified(false);
                }); p.play();

                App.appContextHolder.getRootVBox().setOpacity(1);
                for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                    n.setDisable(false);
                }
            });
            pause.play();
        });

        PropertyBinder.bindAmountOnly(amountTextField);
        PropertyBinder.addComma(amountTextField);
    }
    private void earnPoints() {
        App.appContextHolder.getRootVBox().setOpacity(.50);
        for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
            n.setDisable(true);
        }

        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            String orNumber = receiptTextField.getText();
            String amount = amountTextField.getText();
            ApiResponse apiResponse = earnPointsService.earnPoints(orNumber, amount);
            if (apiResponse.isSuccess()) {
                pointsTextField.setText(null);
                amountTextField.setText(null);
                receiptTextField.setText(null);
                ApiResponse apiResp = memberDetailsService.getCurrentPoints();
                if (apiResp.isSuccess()) {
                    pointsLabel.setText((String) apiResp.getPayload().get("points"));
                }

            }

            Text text = new Text(apiResponse.getMessage());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(receiptTextField.getScene().getWindow());
            alert.setHeaderText("EARN POINTS");
            alert.getDialogPane().setPadding(new Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();

            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
        });
        pause.play();
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
    }
}
