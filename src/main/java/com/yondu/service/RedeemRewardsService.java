package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.RewardDialogController;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.ApiConstants.*;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

/**
 * Created by lynx on 2/21/17.
 */
public class RedeemRewardsService extends BaseService {

    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void initialize() {
        Task task = initializeWorker();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    loadCustomerDetails();
                    renderRewards();
                    enableMenu();
                    hideLoadingScreen();
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                } else {
                    showPrompt(apiResponse.getMessage(), "MEMBER DETAILS", apiResponse.isSuccess());
                    enableMenu();
                    hideLoadingScreen();
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                }


            }
        });
        new Thread(task).start();
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
               try {
                   Customer customer = App.appContextHolder.getCustomer();
                   ApiResponse loginResp = memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                   apiResponse.setMessage(loginResp.getMessage());
                   apiResponse.setErrorCode(loginResp.getErrorCode());
                   apiResponse.setSuccess(loginResp.isSuccess());
               } catch (Exception e) {
                   e.printStackTrace();
               }
                return apiResponse;
            }
        };
    }

    public void redeemRewards(String pin) {
        disableMenu();
        showLoadingScreen();
        Task task = redeemRewardWorker(pin);
        task.setOnSucceeded((Event event) -> {
            ApiResponse apiResponse = (ApiResponse) task.getValue();
            if (apiResponse.isSuccess()) {
                Customer customer = App.appContextHolder.getCustomer();
                Reward reward = App.appContextHolder.getReward();
                Employee employee = App.appContextHolder.getEmployee();
                saveTransaction(TransactionType.REDEEM_REWARDS, customer.getMobileNumber(), employee.getEmployeeName(), null, null, reward.getName());
                VBox rootVBox = App.appContextHolder.getRootContainer();
                Label pointsLabel = (Label) rootVBox.getScene().lookup("#pointsLabel");
                pointsLabel.setText(customer.getAvailablePoints());

            }
            hideLoadingScreen();
            showPrompt(apiResponse.getMessage(), "REDEEM REWARDS", apiResponse.isSuccess());

        });
        new Thread(task).start();

    }

    public Task redeemRewardWorker(String pin) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Customer customer = App.appContextHolder.getCustomer();
                Employee employee = App.appContextHolder.getEmployee();
                Reward reward = App.appContextHolder.getReward();
                Merchant merchant = App.appContextHolder.getMerchant();

                JSONObject requestBody = new JSONObject();
                requestBody.put("merchant_type", merchant.getMerchantType());
                requestBody.put("merchant_key", merchant.getUniqueKey());
                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("pin", pin);
                requestBody.put("reward_id", reward.getId());
                requestBody.put("quantity", reward.getQuantity());

                String url = CMS_URL + WIDGET_REDEEM_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", merchant.getToken());
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Redeem reward successful.");
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        merchant.getRewards().remove(reward);
                        customer.setAvailablePoints((String) data.get("points"));
                    } else {
                        apiResponse.setMessage((String) jsonObject.get("message"));
                    }
                } else {
                    apiResponse.setMessage("Network error.");
                }

                return apiResponse;
            }
        };
    }


    public void renderRewards() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");
        rewardsFlowPane.getChildren().clear();
        Merchant merchant = App.appContextHolder.getMerchant();
        for (Reward reward : merchant.getRewards()) {
            Task task = imageLoaderWorker(reward);
            task.setOnSucceeded((Event e) -> {
                VBox vBox = (VBox) task.getValue();
                rewardsFlowPane.getChildren().add(vBox);
            });
            new Thread(task).start();
        }


    }

    public Task imageLoaderWorker(Reward reward) {
        return new Task() {
            @Override
            protected VBox call() throws Exception {

                VBox vBox = new VBox();
                ImageView imageView = new ImageView(reward.getImageUrl());
                imageView.setFitWidth(350);
                imageView.setFitHeight(200);
                imageView.setOnMouseClicked((MouseEvent e) -> {
                    showRewardsDialog(reward);
                });
                vBox.getChildren().add(imageView);
                Label label = new Label();
                label.getStyleClass().add("label-3");
                label.setText(reward.getName());
                label.getStyleClass().add("lbl-med");
                Label pointsLabel = new Label();
                pointsLabel.getStyleClass().add("label-3");
                pointsLabel.setText(reward.getPointsRequired() + " points");
                pointsLabel.getStyleClass().add("lbl-med");
                pointsLabel.setTextFill(Color.web("red"));

                HBox hBox = new HBox();
                hBox.getChildren().add(label);
                hBox.getChildren().addAll(pointsLabel);
                hBox.setMargin(label, new Insets(0, 0, 0, 20));
                hBox.setMargin(pointsLabel, new Insets(0, 0, 0, 20));
                vBox.getChildren().addAll(hBox);
                vBox.setPrefWidth(200);
                vBox.setMargin(imageView, new Insets(10,10,10,10));
                vBox.setMargin(label, new Insets(10,10,10,10));
                vBox.setPrefHeight(200);
                vBox.setId(reward.getId());
                return vBox;
            }
        };
    }

    private void showRewardsDialog(Reward reward) {
        App.appContextHolder.setReward(reward);
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            try {
                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 350,520);
                scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle(APP_TITLE);
                stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
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

        });
        pause.play();

    }
}
