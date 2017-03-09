package com.yondu.service;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.List;

import static com.yondu.model.constants.AppConfigConstants.*;
import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/22/17.
 */
public class CommonService {

    private ApiService apiService = App.appContextHolder.apiService;

    public void exitMember() {
        App.appContextHolder.setCustomer(null);
        VBox bodyStackPane = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#bodyStackPane");
        App.appContextHolder.routeService.loadContentPage(bodyStackPane, MEMBER_INQUIRY_SCREEN);

        App.appContextHolder.setCurrentState(AppState.MEMBER_INQUIRY);
        updateButtonState();
    }

    public void updateButtonState () {
        VBox sideBarVBox = (VBox) App.appContextHolder.getRootContainer().getScene().lookup("#sideBarVBox");
        List<Node> nodes = sideBarVBox.getChildren();
        for (Node n : nodes) {
        if (n instanceof Button) {
            Button b = (Button) n;
            b.getStyleClass().remove("sidebar-selected");
            }
        }

        AppState appState = App.appContextHolder.getCurrentState();
        Button button = new Button();
        switch (appState) {
            case REGISTRATION: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#registerButton");
                break;
            case MEMBER_INQUIRY: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#memberInquiryButton");
                break;
            case EARN_POINTS: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#givePointsButton");
                break;
            case PAY_WITH_POINTS: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#payWithPointsButton");
                break;
            case REDEEM_REWARDS: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#redeemRewardsButton");
                break;
            case ISSUE_REWARDS:button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#issueRewardsButton");
                break;
            case TRANSACTIONS: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#transactionsButton");
                break;
            case OCR: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#ocrButton");
                break;
            case OFFLINE: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#offlineButton");
                break;
            case GUEST_PURCHASE: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#guestPurchaseButton");
                break;
            case GIVE_STAMPS: button =  (Button) App.appContextHolder.getRootContainer().getScene().lookup("#giveStampsButton");
                break;
        }

        button.getStyleClass().add("sidebar-selected");
    }

    public boolean fetchApiKeys() {
        String merchantKey = this.getMerhant();

        JSONObject payload = new JSONObject();
        payload.put("uniqueKey", merchantKey);

        String url = CMS_URL + TOMCAT_PORT + VALIDATE_MERCHANT_ENDPOINT;
        JSONObject jsonObject = apiService.callWidgetAPI(url, payload, "post");
        if (jsonObject != null) {
            JSONObject data = (JSONObject) jsonObject.get("data");
            MERCHANT_APP_KEY = (String) data.get("merchantApiKey");
            MERCHANT_APP_SECRET = (String) data.get("merchantApiSecret");
            CUSTOMER_APP_KEY =(String) data.get("customerApiKey");
            CUSTOMER_APP_SECRET = (String) data.get("customerApiSecret");
            MERCHANT_TYPE = (String) data.get("merchantType");

            return true;
        }
        return false;
    }

    public  String getMerhant() {
        try {
            File file = new File(RUSH_HOME + DIVIDER + ACTIVATION_FILE);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String merchant = null;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split("=");
                merchant = arr[1];
            }
            br.close();
            return merchant;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void showPrompt(String message, String header) {
        Text text = new Text(message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
        alert.setHeaderText(header);
        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }
}
