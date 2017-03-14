package com.yondu.service;

import com.yondu.App;
import com.yondu.model.OfflineTransaction;
import com.yondu.model.OnlineTransaction;
import com.yondu.model.TransactionType;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by aomine on 3/12/17.
 */
public class BranchTransactionService extends BaseService{

    private ObservableList<OfflineTransaction> offlineData = FXCollections.observableArrayList();
    private ObservableList<OnlineTransaction> onlineData = FXCollections.observableArrayList();
    private ObservableList<OfflineTransaction> filteredOfflineData = FXCollections.observableArrayList();
    private ObservableList<OnlineTransaction> filteredOnlineData = FXCollections.observableArrayList();

    public void initialize() {

        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            loadOfflineTransactions();
            loadTempOfflineTransactions();
            buildStatusFilters();
            renderOfflineTable();


            loadOnlineTransactions();
            renderOnlineTable();

            VBox rootVBox = App.appContextHolder.getRootContainer();
            TabPane vouchersTabPane = (TabPane) rootVBox.getScene().lookup("#transactionsTabPane");
            vouchersTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            for (Tab tab : vouchersTabPane.getTabs()) {
                if (tab.getId().equals("offlineTab")) {
                    tab.setGraphic(new Label("OFFLINE"));
                    tab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                            "    -fx-tab-max-width:200px;\n" +
                            "    -fx-tab-min-height:30px;\n" +
                            "    -fx-tab-max-height:30px;\n" +
                            "    -fx-text-fill: black;\n" +
                            "    -fx-font-size: 17px;");
                } else {
                    tab.setGraphic(new Label("ONLINE"));
                    tab.getGraphic().setStyle("-fx-tab-min-width:200px;\n" +
                            "    -fx-tab-max-width:200px;\n" +
                            "    -fx-tab-min-height:30px;\n" +
                            "    -fx-tab-max-height:30px;\n" +
                            "    -fx-text-fill: white;\n" +
                            "    -fx-font-size: 17px;");
                }

            }
            Button givePointsButton = (Button) rootVBox.getScene().lookup("#givePointsButton");
            if (App.appContextHolder.getEmployee() == null) {
                givePointsButton.setDisable(true);
            }

        });
        pause.play();



    }
    private void renderOfflineTable() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        Pagination offlinePagination = (Pagination) rootVBox.getScene().lookup("#offlinePagination");

        offlinePagination.setPageCount(0);
        offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
    }

    public Node createOfflinePage(int pageIndex) {

        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        return box;
    }

    private TableView<OfflineTransaction> buildTableView() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField searchTextField = (TextField) rootVBox.getScene().lookup("#searchTextField");
        Pagination offlinePagination = (Pagination) rootVBox.getScene().lookup("#offlinePagination");
        MenuButton statusMenuButton = (MenuButton) rootVBox.getScene().lookup("#statusMenuButton");

        TableView<OfflineTransaction> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<OfflineTransaction> finalData = FXCollections.observableArrayList();
        ObservableList<OfflineTransaction> textFilteredData = FXCollections.observableArrayList();

        if (offlineData != null) {
            if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
                String searchTxt = searchTextField.getText().toLowerCase();
                for (OfflineTransaction ot : offlineData) {
                    String temp = "";
                    temp = temp + (ot.getMobileNumber() != null ? ot.getMobileNumber() : "");
                    temp = temp +(ot.getStatus() != null ? ot.getStatus() : "");
                    temp = temp + (ot.getAmount() != null ? ot.getAmount() : "");
                    temp = temp + (ot.getDate() != null ? ot.getDate() : "");
                    temp = temp + (ot.getMessage() != null ? ot.getMessage() : "");
                    temp =  temp  + (ot.getOrNumber() != null ? ot.getOrNumber() : "");
                    if (temp.toLowerCase().contains(searchTxt)) {
                        textFilteredData.add(ot);
                    }
                }
            } else {
                textFilteredData.addAll(offlineData);
            }


            ObservableList<OfflineTransaction> statusFilter = FXCollections.observableArrayList();
            if (!statusMenuButton.getText().equalsIgnoreCase("All")) {
                String status = statusMenuButton.getText().toLowerCase();
                for (OfflineTransaction ot : textFilteredData) {
                    if (ot.getStatus().equalsIgnoreCase(status)) {
                        statusFilter.addAll(ot);
                    }
                }
            } else {
                statusFilter = textFilteredData;
            }

            int maxEntries = 10;
            int pageCount = statusFilter.size() / maxEntries;
            if (pageCount == 0) {
                pageCount = 1;
            } else {
                if (statusFilter.size() % maxEntries > 0) {
                    pageCount++;
                }
            }

            offlinePagination.setPageCount(pageCount);
            int pageIndex = offlinePagination.getCurrentPageIndex();
            ObservableList<OfflineTransaction> indexFilteredData = FXCollections.observableArrayList();
            for (OfflineTransaction transaction : statusFilter) {
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
        filteredOfflineData = finalData;
        buildOfflineColumns(transactionsTableView);
        transactionsTableView.setItems(finalData);
        return transactionsTableView;
    }

    private void buildOfflineColumns(TableView tableView) {


        TableColumn dateCol = new TableColumn("Date");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        TableColumn mobileCol = new TableColumn("Mobile Number");
        mobileCol.setPrefWidth(150);
        mobileCol.setCellValueFactory(
                new PropertyValueFactory<>("mobileNumber"));

        TableColumn orCol = new TableColumn("OR Number");
        orCol.setPrefWidth(150);
        orCol.setCellValueFactory(
                new PropertyValueFactory<>("orNumber"));


        TableColumn statusCol = new TableColumn("Status");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn messageCol = new TableColumn("Message");
        messageCol.setPrefWidth(300);
        messageCol.setCellValueFactory(
                new PropertyValueFactory<>("message"));

        TableColumn amountCol = new TableColumn("Amount");
        amountCol.setPrefWidth(100);
        amountCol.setCellValueFactory(
                new PropertyValueFactory<>("amount"));
        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol, mobileCol,amountCol, orCol, statusCol, messageCol);
    }

    public void  loadOfflineTransactions() {
        offlineData = FXCollections.observableArrayList();
        File file = new File(RUSH_HOME + DIVIDER + OFFLINE_TRANSACTION_FILE);
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
                    offlineData.add(offlineTransaction);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadTempOfflineTransactions() {
        File file = new File(RUSH_HOME + DIVIDER + "temp.txt");
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
                    offlineData.add(offlineTransaction);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void buildStatusFilters() {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        MenuButton statusMenuButton = (MenuButton) rootVBox.getScene().lookup("#statusMenuButton");
        Pagination offlinePagination = (Pagination) rootVBox.getScene().lookup("#offlinePagination");
        TextField searchTextField = (TextField) rootVBox.getScene().lookup("#searchTextField");
        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
            }
        });

        PropertyBinder.bindVirtualKeyboard(searchTextField);

        Label allLabel = new Label("ALL");
        allLabel.setPrefWidth(150);
        allLabel.setWrapText(true);
        MenuItem allItem = new MenuItem();
        allItem.setGraphic(allLabel);
        allItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                statusMenuButton.setText("ALL");
                offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
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
                offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
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
                offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
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
                offlinePagination.setPageFactory((Integer pageIndex) -> createOfflinePage(pageIndex));
            }
        });

        statusMenuButton.getItems().clear();
        statusMenuButton.getItems().addAll(allItem, pendingItem, submittedItem, failedItem);
        statusMenuButton.setText("ALL");
    }



    //online see
    private void renderOnlineTable() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        Pagination onlinePagination = (Pagination) rootVBox.getScene().lookup("#onlinePagination");

        onlinePagination.setPageCount(0);
        onlinePagination.setPageFactory((Integer pageIndex) -> createOnlinePage(pageIndex));
        TextField onlineTextField = (TextField) rootVBox.getScene().lookup("#onlineTextField");
        onlineTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                onlinePagination.setPageFactory((Integer pageIndex) -> createOnlinePage(pageIndex));
            }
        });
        PropertyBinder.bindVirtualKeyboard(onlineTextField);
    }

    public Node createOnlinePage(int pageIndex) {

        VBox box = new VBox();
        box.getChildren().addAll(buildOnlineTableView());
        return box;
    }

    private TableView<OnlineTransaction> buildOnlineTableView() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField onlineTextField = (TextField) rootVBox.getScene().lookup("#onlineTextField");
        Pagination onlinePagination = (Pagination) rootVBox.getScene().lookup("#onlinePagination");

        TableView<OnlineTransaction> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<OnlineTransaction> finalData = FXCollections.observableArrayList();
        ObservableList<OnlineTransaction> textFilteredData = FXCollections.observableArrayList();

        if (onlineData != null) {
            if (onlineTextField.getText() != null && !onlineTextField.getText().isEmpty()) {
                String searchTxt = onlineTextField.getText().toLowerCase();
                for (OnlineTransaction ot : onlineData) {
                    String temp = "";
                    temp = temp + (ot.getMobileNumber() != null ? ot.getMobileNumber() : "");
                    temp = temp +(ot.getDate() != null ? ot.getDate() : "");
                    temp = temp + (ot.getAmount() != null ? ot.getAmount() : "");
                    temp = temp + (ot.getEmployeeName() != null ? ot.getEmployeeName() : "");
                    temp = temp + (ot.getOrNumber() != null ? ot.getOrNumber() : "");
                    temp =  temp  + (ot.getTransactionType() != null ? ot.getTransactionType() : "");
                    temp =  temp  + (ot.getReward() != null ? ot.getReward() : "");
                    if (temp.toLowerCase().contains(searchTxt)) {
                        textFilteredData.add(ot);
                    }
                }
            } else {
                textFilteredData.addAll(onlineData);
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

            onlinePagination.setPageCount(pageCount);
            int pageIndex = onlinePagination.getCurrentPageIndex();
            ObservableList<OnlineTransaction> indexFilteredData = FXCollections.observableArrayList();
            for (OnlineTransaction transaction : textFilteredData) {
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
        filteredOnlineData = finalData;
        buildOnlineColumns(transactionsTableView);
        transactionsTableView.setItems(finalData);
        return transactionsTableView;
    }

    private void buildOnlineColumns(TableView tableView) {


        TableColumn c1 = new TableColumn("Date");
        c1.setPrefWidth(100);
        c1.setCellValueFactory(
                new PropertyValueFactory<>("date"));

        TableColumn c2 = new TableColumn("Transaction Type");
        c2.setPrefWidth(150);
        c2.setCellValueFactory(
                new PropertyValueFactory<>("transactionType"));

        TableColumn c3 = new TableColumn("Employee Name");
        c3.setPrefWidth(150);
        c3.setCellValueFactory(
                new PropertyValueFactory<>("employeeName"));


        TableColumn c4 = new TableColumn("Mobile Number");
        c4.setPrefWidth(100);
        c4.setCellValueFactory(
                new PropertyValueFactory<>("mobileNumber"));

        TableColumn c5 = new TableColumn("Amount");
        c5.setPrefWidth(300);
        c5.setCellValueFactory(
                new PropertyValueFactory<>("amount"));

        TableColumn c6 = new TableColumn("Or Number");
        c6.setPrefWidth(100);
        c6.setCellValueFactory(
                new PropertyValueFactory<>("orNumber"));

        TableColumn c7 = new TableColumn("Reward");
        c7.setPrefWidth(100);
        c7.setCellValueFactory(
                new PropertyValueFactory<>("reward"));
        tableView.getColumns().clear();
        tableView.getColumns().addAll(c1,c2,c3,c4,c5,c6,c7);
    }

    public void loadOnlineTransactions() {
        onlineData = FXCollections.observableArrayList();
        File file = new File(RUSH_HOME + DIVIDER + "branchtransactions.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(line.getBytes());
                    line = new String(decoded);
                    String[] arr = line.split(":");

                    OnlineTransaction ot = new OnlineTransaction();
                    ot.setDate(arr[0].split("=")[1]);
                    String tranType = arr[1].split("=")[1];
                    ot.setTransactionType(TransactionType.valueOf(tranType).getValue());
                    ot.setEmployeeName(arr[2].split("=")[1]);
                    ot.setMobileNumber(arr[3].split("=")[1]);
                    ot.setAmount(arr[4].split("=")[1]);
                    ot.setOrNumber(arr[5].split("=")[1]);
                    ot.setReward(arr[6].split("=")[1]);

                    onlineData.add(ot);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Task exportOfflinWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-YYYY");

                    File file = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + "offlinetransactions-" +sdf.format(date) + ".xls");
                    file.createNewFile();
                    InputStream is = this.getClass().getResourceAsStream("/app/jrxml/offlineReport.jrxml");
                    JasperReport jasperPath = JasperCompileManager.compileReport(is);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, new HashMap<>(), new JRBeanCollectionDataSource(filteredOfflineData));
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.create.custom.palette", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.one.page.per.sheet", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.rows", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.columns", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.white.page.background", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.detect.cell.type", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.size.fix.enabled", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.graphics", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.collapse.row.span", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.border", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.background", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.max.rows.per.sheet", "0");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.wrap.text", "true");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.use.timezone", "false");
                    JRXlsExporter exporter = new JRXlsExporter();

                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                    SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
                    configuration.setOnePagePerSheet(true);
                    configuration.setDetectCellType(true);
                    configuration.setCollapseRowSpan(false);
                    exporter.setConfiguration(configuration);
                    exporter.exportReport();
                } catch (JRException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public void exportOfflineReport() {

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = exportOfflinWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    enableMenu();
                    showPrompt("Export successful.", "EXPORT OFFLINE REPORT");
                }
            });
            new Thread(task).start();
        });
        pause.play();
    }

    public Task exportOnlineWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-YYYY");

                    File file = new File(System.getenv("USERPROFILE") + "\\Desktop\\" + "onlinetransactions-" +sdf.format(date) + ".xls");
                    file.createNewFile();
                    InputStream is = this.getClass().getResourceAsStream("/app/jrxml/onlineReport.jrxml");
                    JasperReport jasperPath = JasperCompileManager.compileReport(is);
                    JasperPrint jasperPrint = JasperFillManager.fillReport(jasperPath, new HashMap<>(), new JRBeanCollectionDataSource(filteredOnlineData));
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.create.custom.palette", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.one.page.per.sheet", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.rows", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.remove.empty.space.between.columns", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.white.page.background", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.detect.cell.type", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.size.fix.enabled", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.graphics", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.collapse.row.span", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.border", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.ignore.cell.background", "false");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.max.rows.per.sheet", "0");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.wrap.text", "true");
                    jasperPrint.setProperty("net.sf.jasperreports.export.xls.use.timezone", "false");
                    JRXlsExporter exporter = new JRXlsExporter();

                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file));
                    SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
                    configuration.setOnePagePerSheet(true);
                    configuration.setDetectCellType(true);
                    configuration.setCollapseRowSpan(false);
                    exporter.setConfiguration(configuration);
                    exporter.exportReport();
                } catch (JRException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    public void exportOnlineReport() {

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = exportOnlineWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    enableMenu();
                    showPrompt("Export successful.", "EXPORT ONLINE REPORT");
                }
            });
            new Thread(task).start();
        });
        pause.play();
    }

}
