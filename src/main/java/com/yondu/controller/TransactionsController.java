package com.yondu.controller;

import com.yondu.App;
import com.yondu.service.TransactionService;
import com.yondu.utils.PropertyBinder;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/7/17.
 */
public class TransactionsController implements Initializable {
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
    public Pagination transactionPagination;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button exitButton;

    private TransactionService transactionService = App.appContextHolder.transactionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        PropertyBinder.bindVirtualKeyboard(searchTextField);

        transactionService.initialize();

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                transactionPagination.setPageFactory((Integer pageIndex) -> transactionService.createTransactionPage(pageIndex));
            }
        });

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            App.appContextHolder.commonService.exitMember();
        });
    }

}
