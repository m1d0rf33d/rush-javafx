package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Transaction;
import com.yondu.service.ApiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.ApiFieldContants.*;

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
    public TableView<Transaction> transactionsTableView;

    private ApiService apiService = new ApiService();
    private Customer customer;

    private final ObservableList<Transaction> transactionsData =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getTransactions();

    }

    public void getTransactions() {
        List params = new ArrayList<>();
        String url = BASE_URL + CUSTOMER_TRANSACTION_ENDPOINT;
        url = url.replace(":customer_uuid", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, params, "get", CUSTOMER_APP_RESOUCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> data = (ArrayList) jsonObject.get("data");
                for (JSONObject json : data) {
                    Transaction transaction = new Transaction();
                    transaction.setReceiptNumber((String) json.get("receipt_no"));
                    transaction.setTransactionType((String) json.get("transaction_type"));
                    transactionsData.add(transaction);
                }

            }
        }

        TableColumn receiptNoCol = new TableColumn("Receipt No");
        receiptNoCol.setMinWidth(150);
        receiptNoCol.setCellValueFactory(
                new PropertyValueFactory<>("receiptNo"));

        TableColumn transactionTypeCol = new TableColumn("Transaction type");
        transactionTypeCol.setMinWidth(150);
        transactionTypeCol.setCellValueFactory(
                new PropertyValueFactory<>("transactionType"));

        transactionsTableView.setItems(transactionsData);
        transactionsTableView.getColumns().clear();
        transactionsTableView.getColumns().addAll(receiptNoCol, transactionTypeCol);

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
