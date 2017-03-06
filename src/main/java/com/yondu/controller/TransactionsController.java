package com.yondu.controller;

import com.yondu.App;
import com.yondu.service.TransactionService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

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
    public Pagination pagination;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button exitButton;

    private TransactionService transactionService = App.appContextHolder.transactionService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        transactionService.initialize();

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
               // pagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
            }
        });
    }

 /*   private Node createPage(int pageIndex) {
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
    }*/


}
