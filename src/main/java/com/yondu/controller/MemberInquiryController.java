package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;
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

    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        viewMemberButton.setOnMouseClicked((MouseEvent e) -> {
            memberDetailsService.viewMember(mobileTextField.getText());
        });

        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindMaxLength(11, mobileTextField);
    }

}
