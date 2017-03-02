package com.yondu.service;

import com.yondu.App;
import com.yondu.controller.RewardDialogController;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import com.yondu.model.Reward;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
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

import static com.yondu.model.constants.AppConfigConstants.*;
import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/21/17.
 */
public class IssueRewardsService extends BaseService {

    private VBox rootVBox = App.appContextHolder.getRootContainer();

    private ApiService apiService = new ApiService();
    private MemberDetailsService memberDetailsService = new MemberDetailsService();

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
                    } else {
                        loadCustomerDetails();
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
                ApiResponse apiResp = memberDetailsService.loginCustomer(customer.getMobileNumber());
                if (apiResp.isSuccess()) {
                    apiResp = memberDetailsService.getActiveVouchers();
                    if (apiResp.isSuccess()) {
                        JSONObject payload = apiResp.getPayload();
                        List<Reward> rewards = (ArrayList) payload.get("rewards");
                        customer.setActiveVouchers(rewards);

                        loadUnclaimedRewards();
                        renderRewards();
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
            for (Reward rew : unclaimedRewards) {
                if (rew.getName().equals(reward.getName())) {
                    reward.setId(rew.getId());
                }
            }
            VBox vBox = new VBox();
            ImageView imageView = new ImageView(reward.getImageUrl());
            imageView.setFitWidth(200);
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

        try {
            Stage stage = new Stage();
            stage.resizableProperty().setValue(Boolean.FALSE);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
            Parent root = fxmlLoader.load();
            RewardDialogController controller = fxmlLoader.getController();
            controller.setReward(reward);
            Scene scene = new Scene(root, 600,400);
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
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                for (JSONObject data : dataJSON) {
                    Reward reward = new Reward();
                    reward.setId((String) data.get("id"));
                    JSONObject innerJSON = (JSONObject) data.get("reward");
                    reward.setName((String) innerJSON.get("name"));
                    unclaimedRewards.add(reward);
                }
            }
        }
    }

    public ApiResponse issueReward(String redeemId) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        Customer customer = App.appContextHolder.getCustomer();
        Employee employee = App.appContextHolder.getEmployee();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("redeem_id", redeemId));
        String url = BASE_URL + CLAIM_REWARDS_ENDPOINT;
        url = url.replace(":customer_id", customer.getUuid());
        url = url.replace(":employee_id", employee.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            apiResponse.setMessage("Issue reward successful.");
            apiResponse.setSuccess(true);
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
    }
}
