package com.yondu.controller;

import com.yondu.App;
import com.yondu.service.GuestPurchaseService;
import com.yondu.service.OcrService;
import com.yondu.utils.PropertyBinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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

    private GuestPurchaseService guestPurchaseService = App.appContextHolder.guestPurchaseService;
    private OcrService ocrService = App.appContextHolder.ocrService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertyBinder.bindVirtualKeyboard(amountTextField);
        PropertyBinder.bindVirtualKeyboard(receiptTextField);
        PropertyBinder.bindVirtualKeyboard(mobileTextField);

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
            String amount = amountTextField.getText().replace(",", "");
            guestPurchaseService.givePoints(mobileNo, orNumber, amount);

        });

        ocrButton.setOnMouseClicked((MouseEvent e) -> {
            ocrService.triggerOCR();
        });
    }


}
