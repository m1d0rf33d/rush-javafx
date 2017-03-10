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

        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {

            Task task = initializeWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        loadCustomerDetails();
                        renderRewards();
                        enableMenu();
                        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    } else {
                        showPrompt(apiResponse.getMessage(), "MEMBER DETAILS");
                        enableMenu();
                        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    }


                }
            });

            new Thread(task).start();
        });
        pause.play();
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse loginResp = memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                if (loginResp.isSuccess()) {
                    ApiResponse rewardsResp = getRewards();
                    if (rewardsResp.isSuccess()) {
                        apiResponse.setSuccess(true);
                    } else {
                        apiResponse.setMessage(apiResponse.getMessage());
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                    return apiResponse;
                }
                return apiResponse;
            }
        };
    }

    public void redeemRewards(String pin) {

        Task task = redeemRewardWorker(pin);
        task.setOnSucceeded((Event event) -> {
            ApiResponse apiResponse = (ApiResponse) task.getValue();
            if (apiResponse.isSuccess()) {
                VBox rootVBox = App.appContextHolder.getRootContainer();
                Label pointsLabel = (Label) rootVBox.getScene().lookup("#pointsLabel");
                JSONObject payload = apiResponse.getPayload();
                pointsLabel.setText((String) payload.get("points"));
            }
            showPrompt(apiResponse.getMessage(), "REDEEM REWARDS");
            enableMenu();
        });
        new Thread(task).start();

    }

    public Task redeemRewardWorker(String pin) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("pin", pin));
                String url = BASE_URL + REDEEM_REWARDS_ENDPOINT;

                Reward reward = App.appContextHolder.getReward();

                Customer customer = App.appContextHolder.getCustomer();
                Employee employee = App.appContextHolder.getEmployee();
                url = url.replace(":customer_id", customer.getUuid());
                url = url.replace(":employee_id", employee.getEmployeeId());
                url = url.replace(":reward_id", reward.getId());
                JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Redeem reward successful.");

                        params = new ArrayList<>();
                        url = BASE_URL + GET_POINTS_ENDPOINT;
                        url = url.replace(":customer_uuid", customer.getUuid());
                        jsonObject = apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
                        if (jsonObject != null) {
                            JSONObject payload = new JSONObject();
                            payload.put("points", jsonObject.get("data"));
                            apiResponse.setSuccess(true);
                            apiResponse.setPayload(payload);
                        } else {
                            apiResponse.setMessage("Network error.");
                        }

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

    public ApiResponse getRewards() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        String url = BASE_URL + GET_REWARDS_MERCHANT_ENDPOINT;
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            List<Reward> rewards = new ArrayList<>();
            List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
            for (JSONObject rewardJSON : dataJSON) {
                Reward reward = new Reward();
                reward.setImageUrl((String) rewardJSON.get("image_url"));
                reward.setDetails((String) rewardJSON.get("details"));
                reward.setName((String) rewardJSON.get("name"));
                reward.setId((String) rewardJSON.get("id"));
                reward.setPointsRequired(String.valueOf((Long) rewardJSON.get("points_required")));
                rewards.add(reward);
            }
            apiResponse.setSuccess(true);
            Merchant merchant = App.appContextHolder.getMerchant();
            merchant.setRewards(rewards);
        } else {
            apiResponse.setMessage("Network error.");
        }

        return apiResponse;
    }

    public void renderRewards() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");

        Merchant merchant = App.appContextHolder.getMerchant();
        List<VBox> vBoxes = new ArrayList<>();
        for (Reward reward : merchant.getRewards()) {
            VBox vBox = new VBox();
            ImageView imageView = new ImageView(reward.getImageUrl());
            imageView.setFitWidth(350);
            imageView.setFitHeight(200);
            imageView.setOnMouseClicked((MouseEvent e) -> {
                showRewardsDialog(reward);
            });
            vBox.getChildren().add(imageView);
            Label label = new Label();
            label.setText(reward.getName());
            label.getStyleClass().add("lbl-med");
            Label pointsLabel = new Label();
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
            vBoxes.add(vBox);
        }
        rewardsFlowPane.getChildren().clear();
        rewardsFlowPane.getChildren().addAll(vBoxes);
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
