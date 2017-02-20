package com.yondu.controller;

import com.yondu.model.Customer;
import com.yondu.model.OfflineTransaction;
import com.yondu.model.Reward;
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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by lynx on 2/9/17.
 */
public class MemberDetailsController implements Initializable {

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
    private Integer MAX_ENTRIES_COUNT = 1;
    private Integer PAGE_COUNT = 0;

    private ObservableList<Reward> masterData =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

    private TableView<Reward> buildTableView() {


        TableView<Reward> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<Reward> textFilteredData = FXCollections.observableArrayList();

        if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
            String searchTxt = searchTextField.getText().toLowerCase();
            for (Reward reward : masterData) {
                if (reward.getDetails().toLowerCase().contains(searchTxt)
                        || reward.getName().toLowerCase().contains(searchTxt)
                        || reward.getQuantity().toLowerCase().contains(searchTxt)) {
                    textFilteredData.addAll(reward);
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
        ObservableList<Reward> indexFilteredData = FXCollections.observableArrayList();
        for (Reward reward : textFilteredData) {
            int objIndex = textFilteredData.indexOf(reward);
            if (objIndex >= (pageIndex * MAX_ENTRIES_COUNT)  && objIndex < ((pageIndex + 1) * MAX_ENTRIES_COUNT)) {
                indexFilteredData.add(reward);
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
        TableColumn dateCol = new TableColumn("Name");
        dateCol.setPrefWidth(200);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        TableColumn mobileCol = new TableColumn("Details");
        mobileCol.setPrefWidth(400);
        mobileCol.setCellValueFactory(
                new PropertyValueFactory<>("details"));

        TableColumn orCol = new TableColumn("Quantity");
        orCol.setPrefWidth(150);
        orCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));


        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol, mobileCol, orCol);
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

        masterData.addAll(customer.getActiveVouchers());
    }
}
