package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Merchant;
import com.yondu.service.CommonService;
import com.yondu.service.MemberDetailsService;
import com.yondu.utils.PropertyBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
    public Pagination activeVouchersPagination;
    @FXML
    public TextField searchTextField;
    @FXML
    public Button exitButton;
    @FXML
    public Pagination transactionsPagination;
    @FXML
    public VBox tableVBox;
    @FXML
    public TabPane vouchersTabPane;
    @FXML
    public Tab transactionsTab;
    @FXML
    public Tab vouchersTab;
    @FXML
    public Label availablePointsLabel;

    private CommonService commonService = App.appContextHolder.commonService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertyBinder.bindVirtualKeyboard(searchTextField);
        transactionsPagination.setPageCount(0);
        activeVouchersPagination.setPageCount(0);

        memberDetailsService.initialize();

        vouchersTabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue == transactionsTab) {
                    vouchersTab.getGraphic().getStyleClass().remove("tab-label-selected");
                    transactionsTab.getGraphic().getStyleClass().add("tab-label-selected");
                } else {
                    transactionsTab.getGraphic().getStyleClass().remove("tab-label-selected");
                    vouchersTab.getGraphic().getStyleClass().add("tab-label-selected");
                }
            }
        });

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });

        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                VBox rootVBox = App.appContextHolder.getRootContainer();
                TabPane vouchersTabPane = (TabPane) rootVBox.getScene().lookup("#vouchersTabPane");
                Tab tab = vouchersTabPane.getSelectionModel().getSelectedItem();
                String tabId = tab.getId();
                if (tabId.equals("transactionsTab")) {
                    transactionsPagination.setPageFactory((Integer pageIndex) -> memberDetailsService.createTransactionsPage(pageIndex));
                } else {
                    activeVouchersPagination.setPageFactory((Integer pageIndex) -> memberDetailsService.createActivateVoucherPage(pageIndex));
                }
            }
        });

        tableVBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                App.appContextHolder.getRootContainer().setMinHeight(600 + newValue.doubleValue());
            }
        });
        Merchant merchant = App.appContextHolder.getMerchant();
        if (merchant.getMerchantType().equals("punchcard")) {
            pointsLabel.setVisible(false);
            availablePointsLabel.setVisible(false);
        }
    }


}
