package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Reward;
import com.yondu.model.Transaction;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/21/17.
 */
public class TransactionService extends BaseService {

    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void initialize() {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = initializeWorker();
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    loadCustomerDetails();
                    renderTransactionTable();
                    enableMenu();
                } else {
                    showPrompt(apiResponse.getMessage(), "TRANSACTIONS");
                    enableMenu();
                }

            });
            new Thread(task).start();
        });
        pause.play();
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {

                ApiResponse apiResponse = new ApiResponse();

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse loginResp = memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                if (loginResp.isSuccess()) {
                    ApiResponse transactionsResp = getTransactions();
                    if (transactionsResp.isSuccess()) {
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage("Network connection error.");
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                    return apiResponse;
                }
                return apiResponse;
            }
        };
    }

    public ApiResponse getTransactions() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List params = new ArrayList<>();
        String url = BASE_URL + CUSTOMER_TRANSACTION_ENDPOINT;

        Customer customer = App.appContextHolder.getCustomer();
        url = url.replace(":customer_uuid", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, params, "get", CUSTOMER_APP_RESOUCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> data = (ArrayList) jsonObject.get("data");

                List<Transaction> transactions = new ArrayList<>();
                for (JSONObject json : data) {
                    Transaction transaction = new Transaction();
                    transaction.setReceiptNumber((String) json.get("receipt_no"));
                    transaction.setTransactionType((String) json.get("transaction_type"));
                    transaction.setPointsEarned((String) json.get("points_earned"));
                    transaction.setCashPaid(String.valueOf(json.get("amount_paid_with_cash")));
                    transaction.setPointsPaid(String.valueOf(json.get("amount_paid_with_points")));
                    transaction.setDate((String) json.get("date"));
                    transactions.add(transaction);
                }
                customer.setTransactions(transactions);
                apiResponse.setSuccess(true);
            }
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }

    private void renderTransactionTable() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        Pagination activeVouchersPagination = (Pagination) rootVBox.getScene().lookup("#transactionPagination");

        activeVouchersPagination.setPageCount(0);
        activeVouchersPagination.setPageFactory((Integer pageIndex) -> createTransactionPage(pageIndex));
    }

    public Node createTransactionPage(int pageIndex) {

        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        return box;
    }

    private TableView<Transaction> buildTableView() {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField searchTextField = (TextField) rootVBox.getScene().lookup("#searchTextField");
        Pagination transactionPagination = (Pagination) rootVBox.getScene().lookup("#transactionPagination");

        TableView<Transaction> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<Transaction> finalData = FXCollections.observableArrayList();
        ObservableList<Transaction> textFilteredData = FXCollections.observableArrayList();
        List<Transaction> transactions = App.appContextHolder.getCustomer().getTransactions();

        if (transactions != null) {
            if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
                String searchTxt = searchTextField.getText().toLowerCase();
                for (Transaction transaction : transactions) {
                    String temp = "";
                    temp = temp + (transaction.getDate() != null ? transaction.getDate() : "");
                    temp = temp +(transaction.getTransactionType() != null ? transaction.getTransactionType() : "");
                    temp = temp + (transaction.getReceiptNumber() != null ? transaction.getReceiptNumber()  : "");
                    temp = temp + (transaction.getPointsEarned() != null ? transaction.getPointsEarned() : "");
                    temp = temp + (transaction.getPointsPaid() != null ? transaction.getPointsPaid() : "");
                    temp =  temp  + (transaction.getCashPaid() != null ? transaction.getCashPaid() : "");
                    if (temp.toLowerCase().contains(searchTxt)) {
                        textFilteredData.add(transaction);
                    }
                }
            } else {
                textFilteredData.addAll(transactions);
            }
            int maxEntries = 10;
            int pageCount = textFilteredData.size() / maxEntries;
            if (pageCount == 0) {
                pageCount = 1;
            } else {
                if (textFilteredData.size() % maxEntries > 0) {
                    pageCount++;
                }
            }

            transactionPagination.setPageCount(pageCount);
            int pageIndex = transactionPagination.getCurrentPageIndex();
            ObservableList<Transaction> indexFilteredData = FXCollections.observableArrayList();
            for (Transaction transaction : textFilteredData) {
                int objIndex = textFilteredData.indexOf(transaction);
                if (objIndex >= (pageIndex * maxEntries)  && objIndex < ((pageIndex + 1) * maxEntries)) {
                    indexFilteredData.add(transaction);
                }
                if (objIndex > ((pageIndex + 1) * maxEntries -1)) {
                    break;
                }
            }
            finalData = indexFilteredData;
        }

        buildTransactionColumns(transactionsTableView);
        transactionsTableView.setItems(finalData);
        return transactionsTableView;
    }

    private void buildTransactionColumns(TableView tableView) {


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
}
