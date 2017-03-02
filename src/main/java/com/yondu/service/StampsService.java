package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.RewardDialogController;
import com.yondu.model.*;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.EARN_STAMPS_ENDPOINT;
import static com.yondu.AppContextHolder.REDEEM_REWARDS_ENDPOINT;
import static com.yondu.model.constants.ApiFieldContants.*;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

/**
 * Created by erwin on 3/1/2017.
 */

public class StampsService extends BaseService  {

    private ApiService apiService = new ApiService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    public ApiResponse earnStamps(String amount) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("amount", amount));
        String url = BASE_URL + EARN_STAMPS_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
        JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                apiResponse.setMessage("Earn stamps successful");
                apiResponse.setSuccess(true);
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network connection error.");
        }
        return apiResponse;
    }

    public ApiResponse redeemStamps(String rewardId, String pin) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("pin", pin));

        String url = BASE_URL + REDEEM_REWARDS_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId()).replace(":customer_id", App.appContextHolder.getCustomerUUID());
        url = url.replace(":reward_id", rewardId);

        JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Redeem reward successful");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        }
        return apiResponse;
    }

    public void loadStamps() {
        Task task = loadStampsWorker();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                VBox rootVBox = App.appContextHolder.getRootVBox();
                FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");
                rewardsFlowPane.getChildren().clear();
                Customer customer = App.appContextHolder.getCustomer();
                Promo promo = customer.getCard().getPromo();
                int stamps = promo.getStamps();
                List<VBox> vBoxes = new ArrayList<>();
                for (Reward reward : promo.getRewards()) {
                    VBox vBox = new VBox();
                    StackPane stackPane = new StackPane();
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(200);

                    if (stamps < reward.getStamps()) {
                        imageView.setImage(new Image(App.appContextHolder.getMerchant().getGrayStampsUrl()));
                    } else {
                        imageView.setImage(new Image(App.appContextHolder.getMerchant().getStampsUrl()));
                        imageView.setOnMouseClicked((MouseEvent e) -> {
                            //   showRewardsDialog(reward);
                        });
                    }

                    ImageView starImg = new ImageView();
                    if (stamps >= reward.getStamps()) {
                        starImg.setImage(new Image(App.class.getResource("/app/images/star-unredeemed.png").toExternalForm()));
                    } else {

                    }
                    starImg.setFitHeight(50);
                    starImg.setFitWidth(50);
                    stackPane.setMargin(starImg, new Insets(70, 0,0,70));
                    stackPane.getChildren().addAll(imageView, starImg);
                    vBox.getChildren().add(stackPane);
                    vBox.setPrefWidth(200);
                    vBox.setMargin(stackPane, new Insets(10,10,10,10));
                    vBox.setPrefHeight(200);
                    vBoxes.add(vBox);
                }
                rewardsFlowPane.getChildren().addAll(vBoxes);
                enableMenu();
            }
        });
        disableMenu();
        new Thread(task).start();
    }


    public Task loadStampsWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                CustomerCard card = memberDetailsService.getCustomerCard();
                Customer customer = App.appContextHolder.getCustomer();
                customer.setCard(card);

                return true;
            }
        };
    }
}
