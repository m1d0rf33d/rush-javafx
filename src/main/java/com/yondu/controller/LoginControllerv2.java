package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.ApiService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import com.yondu.utils.PropertyBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.GET_BRANCHES_ENDPOINT;
import static com.yondu.AppContextHolder.LOGIN_ENDPOINT;
import static com.yondu.model.constants.AppConfigConstants.*;

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
    public Button reconnectButton;
    @FXML
    public Button givePointsButton;
    @FXML
    public TextField mobileTextField;
    @FXML
    public TextField orTextField;
    @FXML
    public TextField amountTextField;
    @FXML
    public HBox rootHBox;
    @FXML
    public ImageView removeImageView;

    private ApiService apiService = new ApiService();
    private RouteService routeService = new RouteService();
    private NotificationService notificationService = new NotificationService();
    private List<JSONObject> branches;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        PropertyBinder.bindNumberOnly(loginTextField);

        App.appContextHolder.setAppState(AppState.LOGIN);

        onlineVBox.setVisible(false);
        offlineVBox.setVisible(false);
        loadingImageView.setVisible(true);
        removeImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/remove.png").toExternalForm()));

        rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        loadingImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));

        loadMerchantBranches();

        loginButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            loginEmployee();
        });
        reconnectButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            routeService.goToSplashScreen((Stage) offlineVBox.getScene().getWindow());
        });
        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            saveOfflineTransaction();
        });

        PropertyBinder.bindAmountOnly(amountTextField);
        PropertyBinder.addComma(amountTextField);


    }

    private void saveOfflineTransaction() {
        try {

            String valid = validateFields();
            if (valid.isEmpty()) {
                File file = new File(System.getenv("RUSH_HOME") + DIVIDER + OFFLINE_TRANSACTION_FILE);
                if (!file.exists()) {
                    file.createNewFile();
                }
                SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
                String date = df.format(new Date());

                PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
                String line = "mobileNumber=" + mobileTextField.getText().replace(":", "")+
                        ":totalAmount=" + amountTextField.getText().replace(":", "") +
                        ":orNumber=" + orTextField.getText().replace(":", "") +
                        ":date=" + date +
                        ":status=Pending:message= ";
                byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(line.getBytes());
                fstream.println(new String(encodedBytes));
                fstream.flush();
                fstream.close();

                mobileTextField.setText(null);
                amountTextField.setText(null);
                orTextField.setText(null);
                showOfflinePersistResult();
            } else {
                disableMenu();
                Text text = new Text(valid);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.initOwner(reconnectButton.getScene().getWindow());
                alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
                alert.getDialogPane().setContent(text);
                alert.getDialogPane().setPrefWidth(400);
                alert.setOnCloseRequest((DialogEvent e) -> {
                    enableMenu();
                });
                alert.show();

                if (alert.getResult() == ButtonType.OK) {
                    alert.close();
                    enableMenu();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showOfflinePersistResult() {
        Text text = new Text("Offline transaction saved.");

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(reconnectButton.getScene().getWindow());
        alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();

        if (alert.getResult() == ButtonType.OK) {
            alert.close();
        }
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
                App.appContextHolder.setBranchName(branchName);
                routeService.goToMenuScreen((Stage) branchComboBox.getScene().getWindow());
            } else if (jsonObject.get("error_code").equals("0x2")) {
                showPinDialog();
            } else {
                String message = (String) jsonObject.get("message");
                notificationService.showMessagePrompt("\n" + message,
                        Alert.AlertType.INFORMATION,
                        null, null,
                        ButtonType.OK);
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
            this.onlineVBox.setVisible(true);
        } else {
            showOfflinePrompt();
           this.offlineVBox.setVisible(true);
        }

        this.loadingImageView.setVisible(false);
    }

    private void showOfflinePrompt() {
        notificationService.showMessagePrompt("\n  No network connection. You are currently in offline mode.  ",
                                            Alert.AlertType.INFORMATION,
                                            null, null,
                                            ButtonType.OK);
    }

    private void showPinDialog() {
        try {
            disableMenu();

            String username = this.loginTextField.getText();
            String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();
            String branchId = "";
            for (JSONObject branch : branches) {
                if (branch.get("name").equals(branchName)) {
                    branchId = (String) branch.get("id");
                    break;
                }
            }


            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            PinController controller = fxmlLoader.getController();
            controller.setRootHBox(rootHBox);
            controller.setLogin(username);
            controller.setOfflineVBox(offlineVBox);
            controller.setOnlineVBox(onlineVBox);
            controller.setBranchId(branchId);
            Scene scene = new Scene(root, 600,400);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(rootHBox.getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    enableMenu();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disableMenu() {
        rootHBox.setOpacity(.50);
        for (Node n : rootHBox.getChildren()) {
            n.setDisable(true);
        }
    }
    public void enableMenu() {
        rootHBox.setOpacity(1);
        for (Node n : rootHBox.getChildren()) {
            n.setDisable(false);
        }
    }

    private String validateFields() {
        String mobileNumber = mobileTextField.getText();
        String orNumber = orTextField.getText();
        String amount = amountTextField.getText();

        String errorMessage = "";
        if (mobileNumber == null || (mobileNumber != null && mobileNumber.isEmpty())) {
            errorMessage = "Mobile number is required.";
        }

        if (orNumber == null || (orNumber != null && orNumber.isEmpty())) {
            errorMessage = "Receipt number is required.";
        }
        if (amount == null || (amount != null && amount.isEmpty())) {
            errorMessage = "Amount is required";
        }
        return errorMessage;
    }
}
