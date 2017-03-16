package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.model.Branch;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.LoginService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.PIN_SCREEN;
import static com.yondu.model.constants.ApiConstants.*;
/**
 * Created by lynx on 2/24/17.
 */
public class LoginOnlineController implements Initializable{

    @FXML
    public ComboBox branchComboBox;
    @FXML
    public Button loginButton;
    @FXML
    public TextField loginTextField;

    private LoginService loginService = App.appContextHolder.loginService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void initAfterLoad() {
        PropertyBinder.bindVirtualKeyboard(loginTextField);
        PropertyBinder.bindNumberOnly(loginTextField);
        PropertyBinder.bindMaxLength(4, loginTextField);

        for (Branch branch : App.appContextHolder.getBranches()) {
            branchComboBox.getItems().add(branch.getName());
        }
        branchComboBox.getSelectionModel().selectFirst();

        loginTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });

        loginButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            String username = this.loginTextField.getText();
            String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();

            Branch selectedBranch = null;
            for (Branch branch : App.appContextHolder.getBranches()) {
                if (branch.getName().equals(branchName)) {
                    selectedBranch = branch;
                    break;
                }
            }
            loginService.loginEmployee(username, selectedBranch, null);

        });
    }
}
