package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.Transaction;
import com.yondu.service.ApiService;
import com.yondu.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
    public Pagination pagination;
    @FXML
    public TextField searchTextField;

    private Customer customer;

    private Integer MAX_ENTRIES_COUNT = 10;
    private Integer PAGE_COUNT = 0;

    private ObservableList<Transaction> masterData =
            FXCollections.observableArrayList();

    private TransactionService transactionService = new TransactionService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        getTransactions();

        PAGE_COUNT = masterData.size() / MAX_ENTRIES_COUNT;
        if (PAGE_COUNT == 0) {
            PAGE_COUNT = 1;
        }
        pagination.setPageCount(PAGE_COUNT);
        pagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                pagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                pagination.setPageCount(PAGE_COUNT);
            }
        });
    }

    private Node createPage(int pageIndex) {
        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        pagination.setPageCount(PAGE_COUNT);
        return box;
    }

    private TableView<Transaction> buildTableView() {


        TableView<Transaction> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<Transaction> textFilteredData = FXCollections.observableArrayList();

        if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
            String searchTxt = searchTextField.getText().toLowerCase();
            for (Transaction transaction : masterData) {
                String str = "";
                str = str + (transaction.getTransactionType() != null ? transaction.getTransactionType().toLowerCase() : "");
                str = str + (transaction.getReceiptNumber() != null ? transaction.getReceiptNumber().toLowerCase() : "");
                if (str.contains(searchTxt)) {
                    textFilteredData.addAll(transaction);
                }
            }
        } else {
            textFilteredData = masterData;
        }

        PAGE_COUNT = textFilteredData.size() / MAX_ENTRIES_COUNT;
        if (PAGE_COUNT == 0) {
            PAGE_COUNT = 1;
        }

        int pageIndex = pagination.getCurrentPageIndex();
        ObservableList<Transaction> indexFilteredData = FXCollections.observableArrayList();
        for (Transaction transaction : textFilteredData) {
            int objIndex = textFilteredData.indexOf(transaction);
            if (objIndex >= (pageIndex * MAX_ENTRIES_COUNT)  && objIndex < ((pageIndex + 1) * MAX_ENTRIES_COUNT)) {
                indexFilteredData.add(transaction);
            }
            if (objIndex > ((pageIndex + 1) * MAX_ENTRIES_COUNT -1)) {
                break;
            }
        }


        buildTableColumns(transactionsTableView);
        transactionsTableView.setItems(indexFilteredData);
        return transactionsTableView;
    }

    private void buildTableColumns(TableView tableView) {
        TableColumn typeCol = new TableColumn("Transaction Type");
        typeCol.setPrefWidth(150);
        typeCol.setCellValueFactory(
                new PropertyValueFactory<>("transactionType"));

        TableColumn receiptCol = new TableColumn("Receipt Number");
        receiptCol.setPrefWidth(150);
        receiptCol.setCellValueFactory(
                new PropertyValueFactory<>("receiptNumber"));

        TableColumn dateCol = new TableColumn("Date");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));


        TableColumn pointsPaidCol = new TableColumn("Points Paid");
        pointsPaidCol.setPrefWidth(150);
        pointsPaidCol.setCellValueFactory(
                new PropertyValueFactory<>("pointsPaid"));

        TableColumn cashPaidCol = new TableColumn("Cash Paid");
        cashPaidCol.setPrefWidth(150);
        cashPaidCol.setCellValueFactory(
                new PropertyValueFactory<>("cashPaid"));

        TableColumn pointsEarnedCol = new TableColumn("Points Earned");
        pointsEarnedCol.setPrefWidth(150);
        pointsEarnedCol.setCellValueFactory(
                new PropertyValueFactory<>("pointsEarned"));


        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol, typeCol, receiptCol, pointsEarnedCol, pointsPaidCol, cashPaidCol);
    }


    public void getTransactions() {

        ApiResponse apiResponse = transactionService.getTransactions();
        if (apiResponse.isSuccess()) {
            masterData.addAll((List<Transaction>) apiResponse.getPayload().get("transactions"));
        } else {

        }

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
