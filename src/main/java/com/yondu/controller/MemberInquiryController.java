package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Branch;
import com.yondu.service.MemberDetailsService;
import com.yondu.utils.PropertyBinder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/9/17.
 */
public class MemberInquiryController implements Initializable {

    @FXML
    public TextField mobileTextField;
    @FXML
    public Button viewMemberButton;

    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        PropertyBinder.bindVirtualKeyboard(mobileTextField);

        viewMemberButton.setOnMouseClicked((MouseEvent e) -> {
            memberDetailsService.viewMember(mobileTextField.getText());
        });

        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindMaxLength(11, mobileTextField);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mobileTextField.requestFocus();
            }
        });

        mobileTextField.setOnKeyPressed((event)-> {
            if(event.getCode() == KeyCode.ENTER) {
                memberDetailsService.viewMember(mobileTextField.getText());
            }
        });
    }

}
