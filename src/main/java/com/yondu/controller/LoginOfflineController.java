package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.model.Branch;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import com.yondu.service.CommonService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.AppContextHolder.CUSTOMER_APP_KEY;
import static com.yondu.AppContextHolder.CUSTOMER_APP_SECRET;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/24/17.
 */
public class LoginOfflineController implements Initializable {
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

    private ApiService apiService = new ApiService();
    private CommonService commonService = new CommonService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Event handlers for clickable nodes
        mobileTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });
        //Event handlers for clickable nodes
        orTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });
        //Event handlers for clickable nodes
        amountTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });

        PropertyBinder.bindAmountOnly(amountTextField);
        PropertyBinder.addComma(amountTextField);

        reconnectButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            ImageView img = new ImageView(new Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));
            img.setFitHeight(150);
            img.setFitWidth(150);
            StackPane bodyStackPane = (StackPane) App.appContextHolder.getLoginHBox().getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            bodyStackPane.getChildren().add(img);

            PauseTransition pause = new PauseTransition(
                    Duration.seconds(1)
            );
            pause.setOnFinished(event -> {
                reconnect();
            });
            pause.play();
        });
        givePointsButton.setOnMouseClicked((MouseEvent e) -> {
            saveOfflineTransaction();
        });
    }

    private void reconnect() {
        StackPane bodyStackPane = (StackPane) App.appContextHolder.getLoginHBox().getScene().lookup("#bodyStackPane");
        if (commonService.fetchApiKeys()) {
            JSONArray branches = new JSONArray();
            String url = BASE_URL + GET_BRANCHES_ENDPOINT;
            java.util.List<NameValuePair> params = new ArrayList<>();
            JSONObject jsonObject = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            if (jsonObject != null) {
                List<JSONObject> data = (ArrayList) jsonObject.get("data");
                for (JSONObject json : data) {
                    Branch branch = new Branch();
                    branch.setId((String) json.get("id"));
                    branch.setName((String) json.get("name"));
                    branches.add(branch);
                }
                try {
                    VBox numbersVBox = (VBox) App.appContextHolder.getLoginHBox().getScene().lookup("#numbersVBox");
                    numbersVBox.setVisible(true);
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_ONLINE_FXML));
                    Parent root = fxmlLoader.load();
                    LoginOnlineController controller = fxmlLoader.getController();
                    controller.setBranches(branches);
                    bodyStackPane.getChildren().clear();
                    bodyStackPane.getChildren().add(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                promptOffline();
            }
        } else {
            promptOffline();
        }

    }

    private void promptOffline() {
        Text text = new Text("Network connection error.");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(App.appContextHolder.getLoginHBox().getScene().getWindow());
        alert.setHeaderText("LOGIN");
        alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();

        try {

            StackPane bodyStackPane = (StackPane) App.appContextHolder.getLoginHBox().getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfigConstants.LOGIN_OFFLINE_FXML));
            Parent root = fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveOfflineTransaction() {
        try {

            String valid = validateFields();
            if (valid.isEmpty()) {
                File file = new File(RUSH_HOME+ DIVIDER + OFFLINE_TRANSACTION_FILE);
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
                valid = "Offline transaction saved.";
            }
            Text text = new Text(valid);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(reconnectButton.getScene().getWindow());
            alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();
        } catch (IOException e) {
            e.printStackTrace();
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
