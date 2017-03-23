package com.yondu.service;

import com.yondu    .App;
import com.yondu.controller.RewardDialogController;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
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

/**
 * Created by erwin on 3/1/2017.
 */

public class StampsService extends BaseService  {

    private ApiService apiService = App.appContextHolder.apiService;
    private MemberDetailsService memberDetailsService = App.appContextHolder.memberDetailsService;

    public void initialize() {

        Task task = stampsInitWorker();
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (!apiResponse.isSuccess()) {
                    showPrompt(apiResponse.getMessage(), "EARN STAMPS", apiResponse.isSuccess());
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    enableMenu();
                } else {
                    loadCustomerDetails();
                    loadRewards();
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                    Merchant merchant = App.appContextHolder.getMerchant();
                    Customer customer = App.appContextHolder.getCustomer();
                    VBox vbox = App.appContextHolder.getRootContainer();
                    if (!merchant.getMerchantClassification().equals("BASIC")) {
                        Label accountNumberLabel = (Label) vbox.getScene().lookup("#accountNumberLabel");
                        Label accountNameLabel = (Label) vbox.getScene().lookup("#accountNameLabel");
                        accountNumberLabel.setText(customer.getAccountNumber());
                        accountNameLabel.setText(customer.getAccountName());
                    }
                    enableMenu();
                }
            }
        });
        new Thread(task).start();


    }

    public Task stampsInitWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse resp = App.appContextHolder.memberDetailsService.loginCustomer(customer.getMobileNumber(), App.appContextHolder.getCurrentState());
                if (resp.isSuccess()) {
                    apiResponse.setSuccess(true);

                } else {
                    apiResponse.setMessage("Network connection error");
                }
                return apiResponse;
            }
        };
    }

    public void loadRewards() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        FlowPane rewardsFlowPane = (FlowPane) rootVBox.getScene().lookup("#rewardsFlowPane");
        rewardsFlowPane.getChildren().clear();
        Customer customer = App.appContextHolder.getCustomer();
        Promo promo = customer.getCard().getPromo();
        Long stamps = customer.getCard().getPromo().getStamps();
        Long customerStampCount = customer.getCard().getStampCount();
        List<VBox> vBoxes = new ArrayList<>();
        for (int x=0; x < stamps; x++) {

            VBox vBox = new VBox();
            StackPane stackPane = new StackPane();
            ImageView imageView = new ImageView();
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);

            if (customerStampCount < x) {
                imageView.setImage(new Image(App.appContextHolder.getMerchant().getGrayStampsUrl()));
            } else {
                imageView.setImage(new Image(App.appContextHolder.getMerchant().getStampsUrl()));
            }

            stackPane.getChildren().addAll(imageView);

            for (Reward reward : promo.getRewards()) {
                if (reward.getStamps() == x) {
                    ImageView starImg = new ImageView();
                    starImg.setImage(new Image(App.class.getResource("/app/images/star-unredeemed.png").toExternalForm()));
                    for (Reward rew: customer.getCard().getRewards()) {
                        if (rew.getId().equals(reward.getId())) {
                            starImg.setImage(new Image(App.class.getResource("/app/images/star-redeemed.png").toExternalForm()));
                            reward.setDate(rew.getDate());
                        }
                    }
                    starImg.setFitHeight(50);
                    starImg.setFitWidth(50);
                    stackPane.setMargin(starImg, new Insets(120, 0,0,120));
                    stackPane.getChildren().add(starImg);

                    imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            showRewardsDialog(reward);
                        }
                    });
                }
            }


            vBox.getChildren().add(stackPane);
            vBox.setPrefWidth(200);
            vBox.setMargin(stackPane, new Insets(10,10,10,10));
            vBox.setPrefHeight(200);
            vBoxes.add(vBox);
        }
        rewardsFlowPane.getChildren().addAll(vBoxes);
    }
    private void showRewardsDialog(Reward reward) {
        App.appContextHolder.setReward(reward);
        App.appContextHolder.getRootContainer().setOpacity(.50);
        for (Node n : App.appContextHolder.getRootContainer().getChildren()) {
            n.setDisable(true);
        }

        try {
            Stage stage = new Stage();
            stage.resizableProperty().setValue(Boolean.FALSE);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/rewards-details.fxml"));
            Parent root = fxmlLoader.load();
            RewardDialogController controller = fxmlLoader.getController();
            Scene scene = new Scene(root, 350,520);
            stage.setScene(scene);
            stage.setTitle("Rush POS Sync");
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    App.appContextHolder.getRootContainer().setOpacity(1);
                    for (Node n : App.appContextHolder.getRootContainer().getChildren()) {
                        n.setDisable(false);
                    }
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void earnStamps(String amount) {

        Task task = earnStampsWorker(amount);
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                ApiResponse apiResponse = (ApiResponse) task.getValue();
                if (apiResponse.isSuccess()) {
                    JSONObject payload = apiResponse.getPayload();
                    Long stampCount = (Long) payload.get("stamp_count");
                    Customer customer = App.appContextHolder.getCustomer();
                    CustomerCard card = customer.getCard();
                    card.setStampCount(stampCount);
                    loadRewards();
                }
                showPrompt(apiResponse.getMessage(), "EARN STAMPS", apiResponse.isSuccess());

            }
        });
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            new Thread(task).start();
        });
        pause.play();

    }

    public Task earnStampsWorker(String amount) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setSuccess(false);

                    Employee employee = App.appContextHolder.getEmployee();
                    Customer customer = App.appContextHolder.getCustomer();
                    Merchant merchant = App.appContextHolder.getMerchant();

                    JSONObject requestBody = new JSONObject();
                    requestBody.put("employee_id", employee.getEmployeeId());
                    requestBody.put("customer_id", customer.getUuid());
                    requestBody.put("merchant_key", merchant.getUniqueKey());
                    requestBody.put("amount", amount);

                    String url = CMS_URL + EARN_STAMP_ENDPOINT;

                    String token = merchant.getToken();
                    JSONObject jsonObject =  apiService.callWidget(url, requestBody.toJSONString(), "post", token);
                    if (jsonObject != null) {

                        String errorCode = (String) jsonObject.get("error_code");
                        if (errorCode.equals("0x0")) {
                            JSONObject payload = new JSONObject();
                            JSONObject data = (JSONObject) jsonObject.get("data");
                            payload.put("stamp_count", data.get("stamp_count"));

                            apiResponse.setPayload(payload);
                            apiResponse.setMessage("Earn stamps successful");
                            apiResponse.setSuccess(true);
                        } else {
                            apiResponse.setMessage((String) jsonObject.get("message"));
                        }
                    } else {
                        apiResponse.setMessage("Network connection error.");
                    }

                    return apiResponse;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public void redeemReward(String pin) {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = redeemRewardWorker(pin);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        loadRewards();
                    }
                    showPrompt(apiResponse.getMessage(), "REDEEM REWARD", apiResponse.isSuccess());

                }
            });
            new Thread(task).start();
        });
        pause.play();
    }

    public Task redeemRewardWorker(String pin) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Employee employee = App.appContextHolder.getEmployee();
                Merchant merchant = App.appContextHolder.getMerchant();
                Customer customer = App.appContextHolder.getCustomer();
                Reward reward = App.appContextHolder.getReward();

                JSONObject payload = new JSONObject();
                payload.put("employee_id", employee.getEmployeeId());
                payload.put("merchant_key", merchant.getUniqueKey());
                payload.put("customer_id", customer.getUuid());
                payload.put("reward_id", reward.getId());
                payload.put("pin", pin);

                String url = CMS_URL + REDEEM_STAMP_ENDPOINT;
                JSONObject jsonObject = apiService.callWidget(url, payload.toJSONString(), "post", merchant.getToken());
                if (jsonObject != null) {

                    if (jsonObject.get("error_code").equals("0x0")) {
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Redeem reward successful");
                        JSONObject customerCard = (JSONObject) data.get("customer_card");
                        CustomerCard card = new CustomerCard();

                        JSONObject promoJSON = (JSONObject) customerCard.get("promo");
                        List<JSONObject> rewJSON = (ArrayList) promoJSON.get("rewards");
                        List<Reward> rewards = new ArrayList<>();
                        for (JSONObject json : rewJSON) {
                            Reward rew = new Reward();
                            rew.setId((String) json.get("id"));
                            rew.setName((String) json.get("name"));
                            rew.setDetails((String) json.get("details"));
                            rew.setStamps(((Long) json.get("stamps")).intValue());
                            rew.setImageUrl((String) json.get("image_url"));
                            rewards.add(rew);
                        }

                        Promo promo = new Promo();
                        promo.setRewards(rewards);
                        promo.setStamps((Long) promoJSON.get("stamps"));
                        card.setPromo(promo);
                        card.setStampCount((Long) customerCard.get("stamps"));
                        customer.setCard(card);

                        List<Reward> redeemedRewards = new ArrayList<>();
                        List<JSONObject> rewardsList = (ArrayList) customerCard.get("rewards");
                        for (JSONObject json :rewardsList) {
                            Reward rew = new Reward();
                            rew.setId((String) json.get("reward_id"));
                            rew.setName((String) json.get("reward_name"));
                            rew.setDate((String) json.get("date"));
                            rew.setImageUrl((String) json.get("reward_image_url"));
                            redeemedRewards.add(rew);
                        }
                        card.setRewards(redeemedRewards);
                        customer.setCard(card);

                    } else {
                        apiResponse.setMessage((String) jsonObject.get("message"));
                    }

                } else {
                    apiResponse.setMessage("Network connection error");
                }


                return apiResponse;
            }
        };
    }

}
