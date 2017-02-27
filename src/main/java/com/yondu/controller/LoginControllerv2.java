package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Branch;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.ApiService;
import com.yondu.service.CommonService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.geometry.Insets;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/6/17.
 */
public class LoginControllerv2 implements Initializable {

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public HBox rootHBox;
    @FXML
    public ImageView removeImageView;
    @FXML
    public StackPane bodyStackPane;
    @FXML
    public Button oneButton;
    @FXML
    public Button twoButton;
    @FXML
    public Button threeButton;
    @FXML
    public Button fourButton;
    @FXML
    public Button fiveButton;
    @FXML
    public Button sixButton;
    @FXML
    public Button sevenButton;
    @FXML
    public Button eightButton;
    @FXML
    public Button nineButton;
    @FXML
    public Button zeroButton;
    @FXML
    public Button dotButton;
    @FXML
    public Button removeButton;
    @FXML
    public VBox numbersVBox;

    private ApiService apiService = new ApiService();
    private List<Branch> branches;
    private CommonService commonService = new CommonService();

    public LoginControllerv2() {
        if (System.getProperty("os.name").contains("Windows")) {
            DIVIDER = "\\";
        } else {
            DIVIDER = "//";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        App.appContextHolder.setAppState(AppState.LOGIN);
        App.appContextHolder.setLoginHBox(rootHBox);

        removeImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/remove.png").toExternalForm()));
        rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        ImageView img = new ImageView();
        img.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));
        img.setFitWidth(150);
        img.setFitHeight(150);
        img.getStyleClass().add("loading-img");
        bodyStackPane.getChildren().clear();
        bodyStackPane.getChildren().addAll(img);
        bodyStackPane.setPadding(new Insets(30,0,0,0));

        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {
            loadEndpointsFromConfig();

            if (commonService.fetchApiKeys()) {

                loadMerchantBranches();
                loadOnline();
            } else {
                loadOffline();
            }
        });
        pause.play();

        PropertyBinder.setNumberButtonClick(oneButton, "1");
        PropertyBinder.setNumberButtonClick(twoButton, "2");
        PropertyBinder.setNumberButtonClick(threeButton, "3");
        PropertyBinder.setNumberButtonClick(fourButton, "4");
        PropertyBinder.setNumberButtonClick(fiveButton, "5");
        PropertyBinder.setNumberButtonClick(sixButton, "6");
        PropertyBinder.setNumberButtonClick(sevenButton, "7");
        PropertyBinder.setNumberButtonClick(eightButton, "8");
        PropertyBinder.setNumberButtonClick(nineButton, "9");
        PropertyBinder.setNumberButtonClick(zeroButton, "0");
        PropertyBinder.setNumberButtonClick(dotButton, ".");

        removeButton.setOnMouseClicked((MouseEvent e) -> {
            TextField loginTextField = (TextField) App.appContextHolder.getLoginHBox().getScene().lookup("#loginTextField");
            if (loginTextField.getText() != null) {
                String subStr = loginTextField.getText().substring(0, loginTextField.getText().length() -1);
                loginTextField.setText(subStr);
            }
        });

    }

    private void loadEndpointsFromConfig() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(API_PROPERTIES);
            if (inputStream != null) {
                prop.load(inputStream);
                inputStream.close();
            } else {
                throw new FileNotFoundException("property file api.properties not found in the classpath");
            }
            BASE_URL = prop.getProperty("base_url");
            REGISTER_ENDPOINT = prop.getProperty("register_endpoint");
            MEMBER_LOGIN_ENDPOINT = prop.getProperty("member_login_endpoint");
            POINTS_CONVERSION_ENDPOINT = prop.getProperty("points_conversion_endpoint");
            GIVE_POINTS_ENDPOINT = prop.getProperty("give_points_endpoint");
            GET_POINTS_ENDPOINT = prop.getProperty("get_points_endpoint");
            PAY_WITH_POINTS_ENDPOINT = prop.getProperty("pay_points_endpoint");
            GET_REWARDS_ENDPOINT = prop.getProperty("get_rewards_endpoint");
            REDEEM_REWARDS_ENDPOINT = prop.getProperty("redeem_rewards_endpoint");
            UNCLAIMED_REWARDS_ENDPOINT = prop.getProperty("unclaimed_rewards_endpoint");
            CLAIM_REWARDS_ENDPOINT = prop.getProperty("claim_rewards_endpoint");
            GET_REWARDS_MERCHANT_ENDPOINT = prop.getProperty("get_rewards_merchant_endpoint");
            CUSTOMER_REWARDS_ENDPOINT = prop.getProperty("customer_rewards_endpoint");
            CUSTOMER_TRANSACTION_ENDPOINT = prop.getProperty("customer_transactions_endpoint");
            GET_BRANCHES_ENDPOINT = prop.getProperty("get_branches_endpoint");
            LOGIN_ENDPOINT = prop.getProperty("login_endpoint");
            AUTHORIZATION_ENDPOINT = prop.getProperty("authorization_endpoint");
            MERCHANT_DESIGNS_ENDPOINT = prop.getProperty("merchant_designs_endpoint");
            MERCHANT_SETTINGS_ENDPOINT = prop.getProperty("merchant_settings_endpoint");
            EARN_GUEST_ENDPOINT = prop.getProperty("earn_guest_endpoint");

            CMS_URL = prop.getProperty("cms_url");
            TOMCAT_PORT = prop.getProperty("tomcat_port");
            OAUTH_SECRET = prop.getProperty("oauth_secret");
            OAUTH_ENDPOINT = prop.getProperty("oauth_endpoint");
            VALIDATE_MERCHANT_ENDPOINT = prop.getProperty("validate_merchant_endpoint");
            ACCESS_ENDPOINT = prop.getProperty("access_endpoint");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMerchantBranches() {
        branches = new JSONArray();

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
        } else {
            loadOffline();
        }
    }

    private  void loadOnline() {
        try {
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
    }

    private void loadOffline() {
        try {
            Text text = new Text("Network connection error.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(oneButton.getScene().getWindow());
            alert.setHeaderText("LOGIN");
            alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();

            numbersVBox.setVisible(false);

            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfigConstants.LOGIN_OFFLINE_FXML));
            Parent root = fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
