package com.yondu.controller;

import com.yondu.App;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by aomine on 3/12/17.
 */
public class BranchController implements Initializable {

    @FXML
    public TextField searchTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        App.appContextHolder.branchTransactionService.initialize();




    }
}
