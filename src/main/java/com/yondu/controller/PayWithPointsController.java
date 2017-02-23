package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.PointsRule;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.*;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.MEMBER_INQUIRY_SCREEN;
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
    @FXML
    public Button ocrButton;
    @FXML
    public Button exitButton;

    private Customer customer;
    private RouteService routeService = new RouteService();
    private EarnPointsService earnPointsService = new EarnPointsService();
    private OcrService ocrService = new OcrService();
    private CommonService commonService = new CommonService();
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });


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
                        ((Stage) App.appContextHolder.getRootVBox().getScene().getWindow()).setIconified(false);
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
                    App.appContextHolder.getRootVBox().setOpacity(1);
                    for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                        n.setDisable(false);
                    }

                }); p.play();

            });
            pause.play();
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
