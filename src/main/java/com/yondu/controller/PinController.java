package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.PointsRule;
import com.yondu.model.Reward;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.*;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;

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

    private TextField receiptTextField;
    private TextField amountTextField;
    private TextField pointsTextField;
    private Label pointsLabel;
    private Label pesoValueLabel;

    private VBox onlineVBox;
    private VBox offlineVBox;
    private HBox rootHBox;
    private String login;
    private String branchId;
    private ApiService apiService = new ApiService();
    private RouteService routeService = new RouteService();
    private NotificationService notificationService = new NotificationService();
    private RedeemRewardsService redeemRewardsService = new RedeemRewardsService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    private Reward reward;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        submitButton.setOnMouseClicked((MouseEvent e) -> {
            ((Stage) pinTextField.getScene().getWindow()).close();
            AppState state = App.appContextHolder.getAppState();
            if (App.appContextHolder.getAppState().equals(AppState.LOGIN)) {
                login();
            } else if (App.appContextHolder.getAppState().equals(AppState.PAY_WITH_POINTS)) {
                payWithPoints();
            } else if (state.equals(AppState.REDEEM_REWARDS)) {
                PauseTransition pause = new PauseTransition(
                        Duration.seconds(.5)
                );
                pause.setOnFinished(event -> {
                    App.appContextHolder.getRootVBox().setOpacity(1);
                    for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                        n.setDisable(false);
                    }

                    ApiResponse apiResponse = redeemRewardsService.redeemRewards(pinTextField.getText(), reward.getId());
                    if (apiResponse.isSuccess()) {
                        Label pointsLabel = (Label) App.appContextHolder.getRootVBox().getScene().lookup("#pointsLabel");
                        ApiResponse apiResp = memberDetailsService.getCurrentPoints();
                        if (apiResp.isSuccess()) {
                            pointsLabel.setText((String) apiResp.getPayload().get("points"));
                        }

                    }
                    Text text = new Text(apiResponse.getMessage());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle(AppConfigConstants.APP_TITLE);
                    alert.initStyle(StageStyle.UTILITY);
                    alert.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
                    alert.setHeaderText("REDEEM REWARDS");
                    alert.getDialogPane().setPadding(new Insets(10,10,10,10));
                    alert.getDialogPane().setContent(text);
                    alert.getDialogPane().setPrefWidth(400);
                    alert.show();
                });
                pause.play();
            }
        });

        cancelButton.setOnMouseClicked((MouseEvent e) -> {
            AppState state = App.appContextHolder.getAppState();
            if (App.appContextHolder.getAppState().equals(AppState.LOGIN)) {
                rootHBox.setOpacity(1);
                for (Node n : rootHBox.getChildren()) {
                    n.setDisable(false);
                }
            }
            if (state.equals(AppState.PAY_WITH_POINTS) || state.equals(AppState.REDEEM_REWARDS)) {
                App.appContextHolder.getRootVBox().setOpacity(1);
                for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                    n.setDisable(false);
                }
            }
            ((Stage) pinTextField.getScene().getWindow()).close();
        });
    }


    private void payWithPoints() {

        String receiptNumber = receiptTextField.getText();
        String amount = amountTextField.getText().replaceAll("[.,]", "");
        String pointsToPay = pointsTextField.getText();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
        params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, receiptNumber));
        params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, amount));
        params.add(new BasicNameValuePair(ApiFieldContants.POINTS, pointsToPay));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, pinTextField.getText()));

        String url = BASE_URL + PAY_WITH_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {

            if (jsonObject.get("error_code").equals("0x0")) {
                ((Stage) pinTextField.getScene().getWindow()).close();
                amountTextField.setText(null);
                receiptTextField.setText(null);
                pointsTextField.setText(null);

               // Double points = Double.parseDouble(menuService.getCurrentPoints());
              //  PointsRule pointsRule = menuService.getPointsRule();
              //  pointsLabel.setText(points.toString());
              //  pesoValueLabel.setText(String.valueOf(points * pointsRule.getRedeemPeso()));
                notificationService.showMessagePrompt("Pay with points successful.", Alert.AlertType.INFORMATION, pinTextField.getScene().getWindow(), App.appContextHolder.getRootVBox(), ButtonType.OK );
            } else if (jsonObject.get("error_code").equals("0x1")){
                String message = "";
                JSONObject errorJSON = (JSONObject) jsonObject.get("errors");
                if (errorJSON.get("amount") != null) {
                    List arr = (ArrayList) errorJSON.get("amount");
                    message = (String) arr.get(0);
                }
                if (errorJSON.get("or_no") != null) {
                    List arr = (ArrayList) errorJSON.get("or_no");
                    message = (String) arr.get(0);
                }
                if (errorJSON.get("points") != null) {
                    List arr = (ArrayList) errorJSON.get("points");
                    message = (String) arr.get(0);
                }
                ((Stage) pinTextField.getScene().getWindow()).close();
                notificationService.showMessagePrompt(message, Alert.AlertType.INFORMATION, pinTextField.getScene().getWindow(), App.appContextHolder.getRootVBox(), ButtonType.OK );
            } else if (jsonObject.get("error_code").equals("0x8")) {
                ((Stage) pinTextField.getScene().getWindow()).close();
                notificationService.showMessagePrompt((String) jsonObject.get("message"), Alert.AlertType.INFORMATION, pinTextField.getScene().getWindow(), App.appContextHolder.getRootVBox(), ButtonType.OK );
            }
        }

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

    public TextField getReceiptTextField() {
        return receiptTextField;
    }

    public void setReceiptTextField(TextField receiptTextField) {
        this.receiptTextField = receiptTextField;
    }

    public TextField getAmountTextField() {
        return amountTextField;
    }

    public void setAmountTextField(TextField amountTextField) {
        this.amountTextField = amountTextField;
    }

    public TextField getPointsTextField() {
        return pointsTextField;
    }

    public void setPointsTextField(TextField pointsTextField) {
        this.pointsTextField = pointsTextField;
    }

    public Label getPointsLabel() {
        return pointsLabel;
    }

    public void setPointsLabel(Label pointsLabel) {
        this.pointsLabel = pointsLabel;
    }

    public Label getPesoValueLabel() {
        return pesoValueLabel;
    }

    public void setPesoValueLabel(Label pesoValueLabel) {
        this.pesoValueLabel = pesoValueLabel;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }
}
