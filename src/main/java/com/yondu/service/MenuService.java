package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.model.Employee;
import com.yondu.model.Merchant;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;
/**
 * Created by lynx on 2/9/17.
 */
public class MenuService extends BaseService{

    private ApiService apiService = App.appContextHolder.apiService;

    public void initialize() {

        Branch branch = App.appContextHolder.getBranch();

        VBox rootVBox = App.appContextHolder.getRootContainer();
        MenuButton employeeMenuButton = (MenuButton) rootVBox.getScene().lookup("#employeeMenuButton");
        employeeMenuButton.setText("Hi! " + App.appContextHolder.getEmployee().getEmployeeName());
        Label branchNameLabel = (Label) rootVBox.getScene().lookup("#branchNameLabel");
        ImageView merchantLogoImageView = (ImageView) rootVBox.getScene().lookup("#merchantLogoImageView");
        branchNameLabel.setText(branch.getName());

        if (App.appContextHolder.getMerchant() != null && App.appContextHolder.getMerchant().getBackgroundUrl() != null) {
            ImageView imageView = new ImageView(new Image(App.appContextHolder.getMerchant().getBackgroundUrl()));
            VBox bodyStackPane = (VBox) rootVBox.getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            bodyStackPane.getChildren().add(imageView);
        }
        if (branch.getLogoUrl() != null) {
            merchantLogoImageView.setImage(new Image(branch.getLogoUrl()));
        }

        loadSideBar();
    }

    private void loadSideBar() {
        VBox rootVBox = App.appContextHolder.getRootContainer();

        List<Button> buttons = new ArrayList<>();
        for (String screen : App.appContextHolder.getEmployee().getScreenAccess()) {
            if (screen.equalsIgnoreCase("REGISTER")) {
                Button registerButton = (Button) rootVBox.getScene().lookup("#registerButton");
                buttons.add(registerButton);
            }
            if (screen.equalsIgnoreCase("MEMBER_PROFILE")) {
                Button memberInquiryButton = (Button) rootVBox.getScene().lookup("#memberInquiryButton");
                buttons.add(memberInquiryButton);
            }
            if (screen.equalsIgnoreCase("GIVE_POINTS")) {
                Button givePointsButton = (Button) rootVBox.getScene().lookup("#givePointsButton");
                buttons.add(givePointsButton);
            }
            if (screen.equalsIgnoreCase("GUEST_PURCHASE")) {
                Button guestPurchaseButton = (Button) rootVBox.getScene().lookup("#guestPurchaseButton");
                buttons.add(guestPurchaseButton);
            }
            if (screen.equalsIgnoreCase("OFFLINE_TRANSACTIONS")) {
                Button offlineButton = (Button) rootVBox.getScene().lookup("#offlineButton");
                buttons.add(offlineButton);
            }
            if (screen.equalsIgnoreCase("PAY_WITH_POINTS")) {
                Button payWithPointsButton = (Button) rootVBox.getScene().lookup("#payWithPointsButton");
                buttons.add(payWithPointsButton);
            }
            if (screen.equalsIgnoreCase("REDEEM_REWARDS")) {
                Button redeemRewardsButton = (Button) rootVBox.getScene().lookup("#redeemRewardsButton");
                buttons.add(redeemRewardsButton);
            }
            if (screen.equalsIgnoreCase("ISSUE_REWARDS")) {
                Button issueRewardsButton = (Button) rootVBox.getScene().lookup("#issueRewardsButton");
                buttons.add(issueRewardsButton);
            }

            if (screen.equalsIgnoreCase("TRANSACTIONS_VIEW")) {
                Button transactionsButton = (Button) rootVBox.getScene().lookup("#transactionsButton");
                buttons.add(transactionsButton);
            }

            if (screen.equalsIgnoreCase("OCR_SETTINGS")) {
                Button ocrButton = (Button) rootVBox.getScene().lookup("#ocrButton");
                buttons.add(ocrButton);
            }
            if (screen.equalsIgnoreCase("GIVE_STAMPS")) {
                Button giveStampsButton = (Button) rootVBox.getScene().lookup("#giveStampsButton");
                buttons.add(giveStampsButton);
            }
        }

        VBox sideBarVBox = (VBox) rootVBox.getScene().lookup("#sideBarVBox");
        sideBarVBox.getChildren().clear();
        for (Button button : buttons) {
            if (button != null) {
                button.setVisible(true);
                sideBarVBox.getChildren().add(button);
            }
        }
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();

                ApiResponse screenAccessResp = getScreenAccess();
                if (screenAccessResp.isSuccess()) {
                    return getMerchantDesign();
                } else {
                    return apiResponse;
                }
            }
        };
    }

    public ApiResponse getMerchantDesign() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        String url = BASE_URL + MERCHANT_DESIGNS_ENDPOINT;
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            JSONObject dataJSON = (JSONObject) jsonObject.get("data");
            JSONObject merchantJSON = (JSONObject) dataJSON.get("merchant");
            Merchant merchant = new Merchant();
            merchant.setBackgroundUrl((String) merchantJSON.get("background_url"));
            merchant.setStampsUrl((String) merchantJSON.get("stamp_url"));
            merchant.setGrayStampsUrl((String) merchantJSON.get("stamp_gray_url"));
            App.appContextHolder.setMerchant(merchant);

            apiResponse.setSuccess(true);
        } else {
            apiResponse.setMessage("Network connection error.");
        }
        return apiResponse;
    }

    private ApiResponse getScreenAccess() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        Employee employee = App.appContextHolder.getEmployee();
        Branch branch = App.appContextHolder.getBranch();

        String url = CMS_URL + TOMCAT_PORT + ACCESS_ENDPOINT;
        url = url.replace(":employee_id", employee.getEmployeeId()).replace(":branch_id", branch.getId());
        JSONObject jsonObj = apiService.callWidgetAPI(url, new JSONObject(), "get");
        if (jsonObj != null) {
            JSONObject dataJson = (JSONObject) jsonObj.get("data");
            List<String> screens = (ArrayList) dataJson.get("access");
            employee.setScreenAccess(screens);
            apiResponse.setSuccess(true);
        } else {
            apiResponse.setMessage("Network connection error.");
        }
        return apiResponse;
    }

}
