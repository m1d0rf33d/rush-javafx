package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.OfflineTransaction;
import com.yondu.model.Reward;
import com.yondu.service.CommonService;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.RouteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.MEMBER_INQUIRY_SCREEN;

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
    @FXML
    public Button exitButton;

    private CommonService commonService = new CommonService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        exitButton.setOnMouseClicked((MouseEvent e) -> {
            commonService.exitMember();
        });



        searchTextField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                pagination.setPageFactory((Integer pageIndex) -> memberDetailsService.createActivateVoucherPage(pageIndex));
            }
        });
    }


}
