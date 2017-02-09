package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewMemberButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            JSONObject jsonObject = menuService.loginCustomer(mobileTextField.getText());
            if (jsonObject.get("customer") != null) {
                loadContentPage(MEMBER_DETAILS_SCREEN, (Customer) jsonObject.get("customer"));
            } else {
                notificationService.showMessagePrompt((String) jsonObject.get("message"), Alert.AlertType.INFORMATION, viewMemberButton.getScene().getWindow(), App.appContextHolder.getRootVBox(), ButtonType.OK);
            }
        });
    }
    private void loadContentPage(String page, Customer customer) {
        try {
            App.appContextHolder.getRootStackPane().getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MEMBER_DETAILS_SCREEN));

            Parent root = (Parent)fxmlLoader.load();
            MemberDetailsController controller = fxmlLoader.<MemberDetailsController>getController();
            controller.setCustomer(customer);
            App.appContextHolder.getRootStackPane().getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
