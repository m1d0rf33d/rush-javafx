package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/9/17.
 */
public class MemberInquiryController implements Initializable {

    @FXML
    public TextField mobileTextField;
    @FXML
    public Button viewMemberButton;


    private MenuService menuService = new MenuService();
    private NotificationService notificationService = new NotificationService();
    private RouteService routeService = new RouteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewMemberButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            JSONObject jsonObject = menuService.loginCustomer(mobileTextField.getText());
            if (jsonObject.get("customer") != null) {
                FXMLLoader fxmlLoader = routeService.loadContentPage(App.appContextHolder.getRootStackPane(), MEMBER_DETAILS_SCREEN);
                MemberDetailsController memberDetailsController = fxmlLoader.getController();
                memberDetailsController.setCustomer((Customer) jsonObject.get("customer"));
            } else {
                notificationService.showMessagePrompt((String) jsonObject.get("message"), Alert.AlertType.INFORMATION, viewMemberButton.getScene().getWindow(), App.appContextHolder.getRootVBox(), ButtonType.OK);
            }
        });
    }

}
