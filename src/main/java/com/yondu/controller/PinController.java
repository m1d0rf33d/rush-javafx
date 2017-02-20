package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.service.ApiService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.LOGIN_ENDPOINT;

/**
 * Created by lynx on 2/16/17.
 */
public class PinController implements Initializable {

    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField pinTextField;
    @FXML
    private Label errorLabel;

    private VBox onlineVBox;
    private VBox offlineVBox;
    private HBox rootHBox;
    private String login;
    private String branchId;
    private ApiService apiService = new ApiService();
    private RouteService routeService = new RouteService();
    private NotificationService notificationService = new NotificationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        submitButton.setOnMouseClicked((MouseEvent e) -> {
            login();
        });

        cancelButton.setOnMouseClicked((MouseEvent e) -> {
            rootHBox.setOpacity(1);
            for (Node n : rootHBox.getChildren()) {
                n.setDisable(false);
            }
            ((Stage) pinTextField.getScene().getWindow()).close();
        });
    }

    private void login() {
        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, login));
        params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, branchId));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, this.pinTextField.getText()));
        String url = BASE_URL + LOGIN_ENDPOINT;
        JSONObject jsonObject = apiService.call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                App.appContextHolder.setEmployeeName(((String) data.get("name")));
                App.appContextHolder.setEmployeeId((String) data.get("id"));
                App.appContextHolder.setBranchId(branchId);
                //Redirect to home page
                routeService.goToMenuScreen((Stage) rootHBox.getScene().getWindow());
                ((Stage) pinTextField.getScene().getWindow()).close();
            } else {
                errorLabel.setText((String) jsonObject.get("message"));
            }
        } else {
            ((Stage) pinTextField.getScene().getWindow()).close();
            notificationService.showMessagePrompt("\n  No network connection. You are currently in offline mode.  ",
                    Alert.AlertType.INFORMATION,
                    null, null,
                    ButtonType.OK);
            rootHBox.setOpacity(1);
            for (Node n : rootHBox.getChildren()) {
                n.setDisable(false);
            }
            offlineVBox.setVisible(true);
            onlineVBox.setVisible(false);
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public HBox getRootHBox() {
        return rootHBox;
    }

    public void setRootHBox(HBox rootHBox) {
        this.rootHBox = rootHBox;
    }

    public VBox getOfflineVBox() {
        return offlineVBox;
    }

    public void setOfflineVBox(VBox offlineVBox) {
        this.offlineVBox = offlineVBox;
    }

    public Button getSubmitButton() {
        return submitButton;
    }

    public void setSubmitButton(Button submitButton) {
        this.submitButton = submitButton;
    }

    public VBox getOnlineVBox() {
        return onlineVBox;
    }

    public void setOnlineVBox(VBox onlineVBox) {
        this.onlineVBox = onlineVBox;
    }
}
