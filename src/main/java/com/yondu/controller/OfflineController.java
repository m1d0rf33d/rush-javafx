package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.OfflineTransaction;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
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
    @FXML
    public Button givePointsButton;

    private ApiService apiService = new ApiService();

    private  ObservableList<OfflineTransaction> masterData =
            FXCollections.observableArrayList();

    private Integer MAX_ENTRIES_COUNT = 10;
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

        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            givePoints();
            loadOfflineTransactions();

            transactionsPagination.setPageFactory((Integer pageIndex) -> createPage(pageIndex));
            transactionsPagination.setPageCount(PAGE_COUNT);
        });

    }

    private void givePoints() {
        File file = new File(System.getenv("RUSH_HOME") + DIVIDER + AppConfigConstants.OFFLINE_TRANSACTION_FILE);

        if (file.exists()) {
            //Read file
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                List<String> transactions = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    String mobileNumber = arr[0].split("=")[1];
                    String totalAmount = arr[1].split("=")[1];
                    String orNumber = arr[2].split("=")[1];
                    String date = arr[3].split("=")[1];
                    String status = arr[4].split("=")[1];
                    String message = arr[5].split("=")[1];

                    OfflineTransaction offlineTransaction = new OfflineTransaction();
                    offlineTransaction.setAmount(totalAmount);
                    offlineTransaction.setMobileNumber(mobileNumber);
                    offlineTransaction.setOrNumber(orNumber);
                    offlineTransaction.setDate(date);
                    offlineTransaction.setStatus(status);
                    offlineTransaction.setMessage(message);

                    if (status.equalsIgnoreCase("Pending")) {
                        offlineTransaction = sendPoints(offlineTransaction);
                    }


                    String l = "mobileNumber=" + offlineTransaction.getMobileNumber()+
                            ":totalAmount=" + offlineTransaction.getAmount() +
                            ":orNumber=" + offlineTransaction.getOrNumber() +
                            ":date=" + offlineTransaction.getDate() +
                            ":status=" + offlineTransaction.getStatus() +
                            ":message=" + offlineTransaction.getMessage();

                    transactions.add(l);

                }

                PrintWriter writer = new PrintWriter(file);
                writer.print("");
                writer.close();

                PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
                for (String trans : transactions) {
                    byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(trans.getBytes());
                    fstream.println(new String(encodedBytes));
                }

                fstream.flush();
                fstream.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private OfflineTransaction sendPoints(OfflineTransaction offlineTransaction) {

        SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
        String date = df.format(new Date());
        offlineTransaction.setDate(date);

        offlineTransaction.setMessage("");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, offlineTransaction.getMobileNumber()));

        String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, offlineTransaction.getOrNumber()));
                params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, offlineTransaction.getAmount().replace(",", "")));
                url = BASE_URL + GIVE_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid", (String) data.get("id"));
                url = url.replace(":employee_id",  App.appContextHolder.getEmployeeId());
                JSONObject json = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                if (json != null) {
                    if (!json.get("error_code").equals("0x0")) {
                        JSONObject error = (JSONObject) json.get("errors");
                        String errorMessage = "";
                        if (error != null) {
                            if (error.get("or_no") != null) {
                                List<String> l = (ArrayList<String>) error.get("or_no");
                                errorMessage = l.get(0);
                            }
                            if (error.get("amount") != null) {
                                List<String> l = (ArrayList<String>) error.get("amount");
                                errorMessage = l.get(0);
                            }
                        }
                        if (json.get("message") != null) {
                            errorMessage = (String) json.get("message");
                        }
                        offlineTransaction.setMessage(errorMessage);
                        offlineTransaction.setStatus("Failed");
                    } else {
                        offlineTransaction.setStatus("Submitted");
                        offlineTransaction.setMessage("Points earned");
                    }
                } else {
                    offlineTransaction.setMessage((String) jsonObject.get("message"));
                    offlineTransaction.setStatus("Failed");
                }
            } else {
                offlineTransaction.setMessage((String) jsonObject.get("message"));
                offlineTransaction.setStatus("Failed");
            }
        }
        return offlineTransaction;
    }

    private Node createPage(int pageIndex) {
        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        return box;
    }

    private TableView<OfflineTransaction> buildTableView() {

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


    public void loadOfflineTransactions() {
        masterData = FXCollections.observableArrayList();
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
                    offlineTransaction.setMessage(arr[5].split("=")[1]);
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
        pendingLabel.setPrefWidth(170);
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
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn messageCol = new TableColumn("Message");
        messageCol.setPrefWidth(250);
        messageCol.setCellValueFactory(
                new PropertyValueFactory<>("message"));


        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol, mobileCol, orCol, statusCol, messageCol);
    }
}
