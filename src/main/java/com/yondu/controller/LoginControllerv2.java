package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.service.ApiService;
import com.yondu.service.RouteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.GET_BRANCHES_ENDPOINT;
import static com.yondu.AppContextHolder.LOGIN_ENDPOINT;

/**
 * Created by lynx on 2/6/17.
 */
public class LoginControllerv2 implements Initializable {

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public ComboBox branchComboBox;
    @FXML
    public Button loginButton;
    @FXML
    public ImageView loadingImageView;
    @FXML
    public TextField loginTextField;
    @FXML
    public VBox onlineVBox;
    @FXML
    public VBox offlineVBox;
    @FXML
    public Label mobileNumberLabel;
    @FXML
    public TextField mobileNumberTextField;

    private ApiService apiService = new ApiService();
    private RouteService routeService = new RouteService();
    private List<JSONObject> branches;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        onlineVBox.setVisible(false);


        rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        loadingImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));
        loadMerchantBranches();

        loginButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            loginEmployee();
        });

    }

    private void loginEmployee() {

        String username = this.loginTextField.getText();
        String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();
        String branchId = "";
        for (JSONObject branch : branches) {
            if (branch.get("name").equals(branchName)) {
                branchId = (String) branch.get("id");
                break;
            }
        }

        //Build request body
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_id", username));
        params.add(new BasicNameValuePair("branch_id", branchId));
        String url = BASE_URL + LOGIN_ENDPOINT;
        JSONObject jsonObject = apiService.call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                App.appContextHolder.setEmployeeName(((String) data.get("name")));
                App.appContextHolder.setEmployeeId((String) data.get("id"));
                App.appContextHolder.setBranchId(branchId);

                routeService.goToHomeScreen((Stage) branchComboBox.getScene().getWindow());
            } else if (jsonObject.get("error_code").equals("0x2")) {
               // showRequirePinModal(event);
            } else {
               // prompt((String) jsonObject.get("message"), event);
            }
        }
        //this.overlayPane.setVisible(false);
    }

    private void loadMerchantBranches() {
        branches = new JSONArray();

        String url = BASE_URL + GET_BRANCHES_ENDPOINT;
        java.util.List<NameValuePair> params = new ArrayList<>();
        JSONObject jsonObject = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            List<JSONObject> data = (ArrayList) jsonObject.get("data");
            for (JSONObject branch : data) {
                branches.add(branch);
                branchComboBox.getItems().add(branch.get("name"));
            }
            branchComboBox.getSelectionModel().selectFirst();
        }
        this.loadingImageView.setVisible(false);
    }
}
