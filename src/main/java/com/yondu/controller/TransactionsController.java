package com.yondu.controller;

import com.yondu.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/7/17.
 */
public class TransactionsController implements Initializable {

    @FXML
    public TableView<Transaction> transactionsTableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Transaction> data =  FXCollections.observableArrayList( new Transaction("1", "EARN"),
                new Transaction("2", "GIVE"));

        TableColumn idColumn = new TableColumn("ID");
        idColumn.setCellValueFactory(
                new PropertyValueFactory<Transaction, String>("id"));
        TableColumn typeColumn = new TableColumn("TYPE");
        typeColumn.setCellValueFactory(
                new PropertyValueFactory<Transaction, String>("type"));

        transactionsTableView.getColumns().clear();
        transactionsTableView.getColumns().addAll(idColumn, typeColumn);
        transactionsTableView.setItems(data);
    }
}
