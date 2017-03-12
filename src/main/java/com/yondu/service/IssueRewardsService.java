package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;
import static com.yondu.model.constants.AppConfigConstants.APP_TITLE;
import static com.yondu.model.constants.AppConfigConstants.ISSUE_REWARDS_SCREEN;
import static com.yondu.model.constants.AppConfigConstants.REWARDS_DIALOG_SCREEN;

/**
 * Created by lynx on 2/21/17.
 */
public class IssueRewardsService extends BaseService {


    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    private List<Reward> unclaimedRewards;

    public void initialize() {
        enableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = initializeWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (!apiResponse.isSuccess()) {
                        showPrompt(apiResponse.getMessage(), "ISSUE REWARDS");
                        enableMenu();
                    } else {
                        loadCustomerDetails();
                        renderRewards();
                        enableMenu();
                    }
                }
            });
            new Thread(task).start();

        });
        pause.play();
    }

    private Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse loginResp = memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                apiResponse.setMessage(loginResp.getMessage());
                apiResponse.setErrorCode(loginResp.getErrorCode());
                apiResponse.setSuccess(loginResp.isSuccess());
                return apiResponse;
            }
        };
    }

    public void renderRewards() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");
        rewardsFlowPane.getChildren().clear();

        Customer customer = App.appContextHolder.getCustomer();
        List<VBox> vBoxes = new ArrayList<>();
        for (Reward reward : customer.getActiveVouchers()) {

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
            vBox.getChildren().addAll(label);
            vBox.setPrefWidth(200);
            vBox.setMargin(imageView, new Insets(10,10,10,10));
            vBox.setMargin(label, new Insets(10,10,10,10));
            vBox.setPrefHeight(200);
            vBoxes.add(vBox);
        }

        rewardsFlowPane.getChildren().addAll(vBoxes);
    }

    private void showRewardsDialog(Reward reward) {

        App.appContextHolder.setReward(reward);

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            VBox rootVBox =  App.appContextHolder.getRootContainer();
            try {
                Stage stage = new Stage();
                stage.resizableProperty().setValue(Boolean.FALSE);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 350,520);
                stage.setScene(scene);
                stage.setTitle(APP_TITLE);
                stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.initOwner(rootVBox.getScene().getWindow());
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


    public void issueReward(String redeemId) {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = issueRewardWorker(redeemId);
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    renderRewards();
                }
                showPrompt(apiResponse.getMessage(), "ISSUE REWARD");
                enableMenu();
            });
            new Thread(task).start();
        });
        pause.play();
    }

    public Task issueRewardWorker(String redeemId) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();

                Customer customer = App.appContextHolder.getCustomer();
                Employee employee = App.appContextHolder.getEmployee();
                Merchant merchant = App.appContextHolder.getMerchant();

                JSONObject requestBody = new JSONObject();
                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("merchant_type", merchant.getMerchantType());
                requestBody.put("merchant_key", merchant.getUniqueKey());
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("redeem_id", redeemId);


                String url = CMS_URL + WIDGET_ISSUE_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", merchant.getToken());
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setMessage("Issue reward successful.");
                        apiResponse.setSuccess(true);

                        List<Reward> rewards = customer.getActiveVouchers();
                        Reward delete = null;
                        for (Reward reward : rewards) {
                            if (reward.getRedeemId().equals(redeemId)) {
                                delete = reward;
                                break;
                            }
                        }
                        rewards.remove(delete);

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
}
