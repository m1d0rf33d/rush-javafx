package com.yondu.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by erwin on 10/11/2016.
 */
public class ActivationController implements Initializable{

    @FXML
    public Button activateBtn;

    @FXML
    public TextField merchantKey;



    @Override
    public void initialize(URL location, ResourceBundle resources) {


        activateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            
        });
    }
}
