package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import com.yondu.model.Reward;
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
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
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
                apiResponse.setSuccess(false);

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse apiResp = memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                if (apiResp.isSuccess()) {
                    apiResp = memberDetailsService.getActiveVouchers();
                    if (apiResp.isSuccess()) {

                        List<Reward> rewards = App.appContextHolder.getCustomer().getActiveVouchers();
                        customer.setActiveVouchers(rewards);
                        apiResponse.setSuccess(true);
                        loadUnclaimedRewards();
                        return apiResponse;
                    }
                } else {
                    return apiResponse;
                }
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
            for (Reward rew : App.appContextHolder.getUnclaimedRewards()) {
                if (rew.getName().equals(reward.getName())) {
                    reward.setId(rew.getId());
                }
            }
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

    public void loadUnclaimedRewards() {

        Employee employee = App.appContextHolder.getEmployee();
        Customer customer = App.appContextHolder.getCustomer();
        String url = BASE_URL + UNCLAIMED_REWARDS_ENDPOINT;
        url = url.replace(":employee_id", employee.getEmployeeId());
        url = url.replace(":customer_id", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                App.appContextHolder.getUnclaimedRewards().clear();
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                for (JSONObject data : dataJSON) {
                    Reward reward = new Reward();
                    reward.setId((String) data.get("id"));
                    JSONObject innerJSON = (JSONObject) data.get("reward");
                    reward.setName((String) innerJSON.get("name"));
                    App.appContextHolder.getUnclaimedRewards().add(reward);
                }
            }
        }
    }

    public void issueReward(String redeemId) {
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = issueRewardWorker(redeemId);
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    App.appContextHolder.memberDetailsService.getActiveVouchers();
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

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("redeem_id", redeemId));
                String url = BASE_URL + CLAIM_REWARDS_ENDPOINT;
                url = url.replace(":customer_id", customer.getUuid());
                url = url.replace(":employee_id", employee.getEmployeeId());
                JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setMessage("Issue reward successful.");
                        apiResponse.setSuccess(true);
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
