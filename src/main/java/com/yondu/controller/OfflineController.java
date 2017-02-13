package com.yondu.controller;

import com.yondu.model.OfflineTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.DIVIDER;
import static com.yondu.model.constants.AppConfigConstants.OFFLINE_TRANSACTION_FILE;

/**
 * Created by lynx on 2/10/17.
 */
public class OfflineController implements Initializable {

    @FXML
    public MenuButton statusMenuButton;
    @FXML
    public Pagination transactionsPagination;
    @FXML
    public TextField searchTextField;

    private  ObservableList<OfflineTransaction> masterData =
            FXCollections.observableArrayList();

    private Integer MAX_ENTRIES_COUNT = 1;
    private Integer PAGE_COUNT = 0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadOfflineTransactions();
        buildStatusFilters();

        statusMenuButton.setText("All");
        PAGE_COUNT = masterData.size() / MAX_ENTRIES_COUNT;
        if (PAGE_COUNT == 0) {
            PAGE_COUNT = 1;
        }

        transactionsPagination.setPageCount(PAGE_COUNT);
        transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                transactionsPagination.setPageCount(PAGE_COUNT);
            }
        });

    }


    private Node createPage(int pageIndex) {
        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        return box;
    }

    private TableView<OfflineTransaction> buildTableView() {

        String status = statusMenuButton.getText();

        TableView<OfflineTransaction> transactionsTableView = new TableView();


        ObservableList<OfflineTransaction> statusFilteredData = FXCollections.observableArrayList();
        for (OfflineTransaction offlineTransaction : masterData) {
          if (status.equalsIgnoreCase("all")) {
              statusFilteredData.add(offlineTransaction);
          } else {
              if (status.equalsIgnoreCase(offlineTransaction.getStatus())) {
                  statusFilteredData.add(offlineTransaction);
              }
          }
        }

        ObservableList<OfflineTransaction> textFilteredData = FXCollections.observableArrayList();
        if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
            String searchTxt = searchTextField.getText().toLowerCase();
            for (OfflineTransaction offlineTransaction : statusFilteredData) {
                if (offlineTransaction.getOrNumber().toLowerCase().contains(searchTxt)
                        || offlineTransaction.getMobileNumber().toLowerCase().contains(searchTxt)
                        || offlineTransaction.getDate().toLowerCase().contains(searchTxt)
                        || offlineTransaction.getStatus().toLowerCase().contains(searchTxt)) {
                    textFilteredData.addAll(offlineTransaction);
                }
            }
        } else {
            textFilteredData = statusFilteredData;
        }
        PAGE_COUNT = textFilteredData.size() / MAX_ENTRIES_COUNT;
        if (PAGE_COUNT == 0) {
            PAGE_COUNT = 1;
        }

        int pageIndex = transactionsPagination.getCurrentPageIndex();
        ObservableList<OfflineTransaction> indexFilteredData = FXCollections.observableArrayList();
        for (OfflineTransaction offlineTransaction : textFilteredData) {
            int objIndex = masterData.indexOf(offlineTransaction);
            if (objIndex >= pageIndex || objIndex < (pageIndex * MAX_ENTRIES_COUNT -1)) {
                indexFilteredData.add(offlineTransaction);
            }
            if (objIndex > (pageIndex * MAX_ENTRIES_COUNT -1)) {
                break;
            }
        }


        buildTableColumns(transactionsTableView);
        transactionsTableView.setItems(textFilteredData);
        return transactionsTableView;
    }

    public void loadOfflineTransactions() {
        File file = new File(System.getenv("RUSH_HOME") + DIVIDER + OFFLINE_TRANSACTION_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    OfflineTransaction offlineTransaction = new OfflineTransaction();
                    offlineTransaction.setMobileNumber(arr[0].split("=")[1]);
                    offlineTransaction.setAmount(arr[1].split("=")[1]);
                    offlineTransaction.setOrNumber(arr[2].split("=")[1]);
                    offlineTransaction.setDate(arr[3].split("=")[1]);
                    offlineTransaction.setStatus(arr[4].split("=")[1]);
                    masterData.add(offlineTransaction);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void buildStatusFilters() {
        Label allLabel = new Label("ALL");
        allLabel.setPrefWidth(150);
        allLabel.setWrapText(true);
        MenuItem allItem = new MenuItem();
        allItem.setGraphic(allLabel);
        allItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                statusMenuButton.setText("ALL");
                transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                transactionsPagination.setPageCount(PAGE_COUNT);
            }
        });

        Label pendingLabel = new Label("PENDING");
        pendingLabel.setPrefWidth(150);
        pendingLabel.setWrapText(true);
        MenuItem pendingItem = new MenuItem();
        pendingItem.setGraphic(pendingLabel);
        pendingItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("PENDING");
                transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                transactionsPagination.setPageCount(PAGE_COUNT);
            }
        });


        Label submittedLabel = new Label("SUBMITTED");
        submittedLabel.setPrefWidth(150);
        submittedLabel.setWrapText(true);
        MenuItem submittedItem = new MenuItem();
        submittedItem.setGraphic(submittedLabel);
        submittedItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("SUBMITTED");
                transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                transactionsPagination.setPageCount(PAGE_COUNT);
            }
        });

        Label failedLabel = new Label("FAILED");
        failedLabel.setPrefWidth(150);
        failedLabel.setWrapText(true);
        MenuItem failedItem = new MenuItem();
        failedItem.setGraphic(failedLabel);
        failedItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("FAILED");
                transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
                transactionsPagination.setPageCount(PAGE_COUNT);
            }
        });

        statusMenuButton.getItems().clear();
        statusMenuButton.getItems().addAll(allItem, pendingItem, submittedItem, failedItem);
    }

    private void buildTableColumns(TableView tableView) {
        TableColumn dateCol = new TableColumn("Date");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        TableColumn mobileCol = new TableColumn("Mobile Number");
        mobileCol.setPrefWidth(150);
        mobileCol.setCellValueFactory(
                new PropertyValueFactory<>("mobileNumber"));

        TableColumn orCol = new TableColumn("OR NUMBER");
        orCol.setPrefWidth(150);
        orCol.setCellValueFactory(
                new PropertyValueFactory<>("orNumber"));


        TableColumn statusCol = new TableColumn("Status");
        statusCol.setPrefWidth(150);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));


        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol, mobileCol, orCol, statusCol);
    }
}
