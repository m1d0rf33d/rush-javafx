package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import com.yondu.model.constants.AppState;
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
import javafx.scene.image.Image;
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
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = initializeWorker();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (!apiResponse.isSuccess()) {
                        showPrompt(apiResponse.getMessage(), "ISSUE REWARDS", apiResponse.isSuccess());
                        hideLoadingScreen();
                    } else {
                        loadCustomerDetails();
                        renderRewards();
                        hideLoadingScreen();
                    }
                    enableMenu();
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
                ApiResponse loginResp = App.appContextHolder.memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
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
        Merchant merchant = App.appContextHolder.getMerchant();
        List<Reward> rewards= new ArrayList<>();
        if (merchant.getMerchantType().equals("punchcard")) {
           if (customer.getCard() != null) {
               rewards = customer.getCard().getRewards();
               if (App.appContextHolder.getCurrentState().equals(AppState.ISSUE_REWARDS)) {
                   List<Reward> forRemoval = new ArrayList<>();
                   for (Reward reward : rewards) {
                       if (reward.getStatus()) {
                           forRemoval.add(reward);
                       }
                   }
                   rewards.removeAll(forRemoval);
               }
           }
        } else {
            rewards = customer.getActiveVouchers();
        }

       if (rewards != null) {
           for (Reward reward : rewards) {
               Task task = imageLoaderWorker(reward);
               task.setOnSucceeded((Event e)-> {
                   VBox vbox = (VBox) task.getValue();
                   rewardsFlowPane.getChildren().add(vbox);
               });
               new Thread(task).start();
           }
       }
    }

    private Task imageLoaderWorker(Reward reward) {
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
                vBox.getChildren().addAll(label);
                vBox.setPrefWidth(200);
                vBox.setMargin(imageView, new Insets(10,10,10,10));
                vBox.setMargin(label, new Insets(10,10,10,10));
                vBox.setPrefHeight(200);
                return vBox;
            }
        };
    }

    private void showRewardsDialog(Reward reward) {

        App.appContextHolder.setReward(reward);

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            VBox rootVBox =  App.appContextHolder.getRootContainer();
            try {
                Stage stage = new Stage();
                stage.resizableProperty().setValue(Boolean.FALSE);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(REWARDS_DIALOG_SCREEN));
                Parent root = fxmlLoader.load();
                Scene scene = new Scene(root, 330,520);
                scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
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
        showLoadingScreen();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = issueRewardWorker(redeemId);
            task.setOnSucceeded((Event e) -> {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    Reward reward = App.appContextHolder.getReward();
                    Customer customer = App.appContextHolder.getCustomer();
                    Employee employee = App.appContextHolder.getEmployee();
                    saveTransaction(TransactionType.ISSUE_REWARD, customer.getMobileNumber(), employee.getEmployeeName(), null, null, reward.getName());
                    VBox rootVBox = App.appContextHolder.getRootContainer();
                    FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");
                    rewardsFlowPane.getChildren().clear();
                    initialize();
                }
                hideLoadingScreen();
                showPrompt(apiResponse.getMessage(), "ISSUE REWARD", apiResponse.isSuccess());

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
                Reward r = App.appContextHolder.getReward();

                JSONObject requestBody = new JSONObject();
                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("merchant_type", merchant.getMerchantType());
                requestBody.put("merchant_key", merchant.getUniqueKey());
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("redeem_id", redeemId);
                requestBody.put("quantity", r.getQuantityToIssue());


                String url = CMS_URL + WIDGET_ISSUE_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", merchant.getToken());
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

    public void issueStampReward(String pin) {

        Task task = issueStampRewardWorker(pin);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    renderRewards();
                }
                showPrompt(apiResponse.getMessage(), "ISSUE REWARD", apiResponse.isSuccess());

            }
        });

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            new Thread(task).start();
        });
        pause.play();
    }


    public Task issueStampRewardWorker(String pin) {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Employee employee = App.appContextHolder.getEmployee();
                Merchant merchant = App.appContextHolder.getMerchant();
                Customer customer = App.appContextHolder.getCustomer();
                Reward reward = App.appContextHolder.getReward();

                JSONObject requestBody = new JSONObject();
                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("customer_id", customer.getUuid());
                requestBody.put("reward_id", reward.getId());
                requestBody.put("merchant_key", merchant.getUniqueKey());
                requestBody.put("pin", pin);

                String url = CMS_URL + ISSUE_STAMP_ENDPOINT;

                JSONObject jsonObject = apiService.callWidget(url, requestBody.toJSONString(), "post", merchant.getToken());
                if (jsonObject != null) {

                    String message = (String) jsonObject.get("message");
                    String errorCode = (String) jsonObject.get("error_code");
                    apiResponse.setMessage(message);
                    apiResponse.setErrorCode(errorCode);

                    if (errorCode.equals("0x0")) {
                        apiResponse.setSuccess(true);

                        List<Reward> rewards = customer.getCard().getRewards();
                        rewards.remove(reward);
                    }

                } else {
                    apiResponse.setMessage("Network connection error");
                }

                return apiResponse;
            }
        };
    }
}
