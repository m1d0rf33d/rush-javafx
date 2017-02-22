package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.GuestPurchaseService;
import com.yondu.service.OcrService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/22/17.
 */
public class GuestPurchaseController implements Initializable {

    @FXML
    public TextField amountTextField;
    @FXML
    public TextField receiptTextField;
    @FXML
    public TextField mobileTextField;
    @FXML
    public Button ocrButton;
    @FXML
    public Button exitButton;
    @FXML
    public Button submitButton;
    @FXML
    public Button clearButton;

    private GuestPurchaseService guestPurchaseService = new GuestPurchaseService();
    private OcrService ocrService = new OcrService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        PropertyBinder.bindMaxLength(11, mobileTextField);
        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindAmountOnly(amountTextField);
        PropertyBinder.addComma(amountTextField);

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            amountTextField.setText(null);
            receiptTextField.setText(null);
            mobileTextField.setText(null);
        });

        submitButton.setOnMouseClicked((MouseEvent e) -> {
            String mobileNo = mobileTextField.getText();
            String orNumber = receiptTextField.getText();
            String amount = amountTextField.getText();
            ApiResponse apiResponse = guestPurchaseService.givePoints(mobileNo, orNumber, amount);
            if (apiResponse.isSuccess()) {
                amountTextField.setText(null);
                receiptTextField.setText(null);
                mobileTextField.setText(null);
            }
            Text text = new Text(apiResponse.getMessage());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
            alert.setHeaderText("GUEST PURCHASE");
            alert.getDialogPane().setPadding(new Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();
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
    }


}
