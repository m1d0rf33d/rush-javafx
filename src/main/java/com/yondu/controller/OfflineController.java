package com.yondu.controller;

import com.yondu.App;
import com.yondu.service.OfflineService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/10/17.
 */
public class OfflineController implements Initializable {

    @FXML
    public MenuButton statusMenuButton;
    @FXML
    public Pagination offlinePagination;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button givePointsButton;

    private OfflineService offlineService = App.appContextHolder.offlineService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        offlineService.initialize();
        buildStatusFilters();

        statusMenuButton.setText("All");

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            offlineService.givePoints();
        });

    }



  /*  private TableView<OfflineTransaction> buildTableView() {

        String status = statusMenuButton.getText();

        TableView<OfflineTransaction> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

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
            if (objIndex >= pageIndex && objIndex < ((pageIndex + 1) * MAX_ENTRIES_COUNT -1)) {
                indexFilteredData.add(offlineTransaction);
            }
            if (objIndex > ((pageIndex + 1) * MAX_ENTRIES_COUNT -1)) {
                break;
            }
        }


        buildTableColumns(transactionsTableView);
        transactionsTableView.setItems(indexFilteredData);
        return transactionsTableView;
    }
*/



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
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        Label pendingLabel = new Label("PENDING");
        pendingLabel.setPrefWidth(170);
        pendingLabel.setWrapText(true);
        MenuItem pendingItem = new MenuItem();
        pendingItem.setGraphic(pendingLabel);
        pendingItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                statusMenuButton.setText("PENDING");
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
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
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
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
                offlinePagination.setPageFactory((Integer pageIndex) -> offlineService.createOfflinePage(pageIndex));
            }
        });

        statusMenuButton.getItems().clear();
        statusMenuButton.getItems().addAll(allItem, pendingItem, submittedItem, failedItem);
    }

}
