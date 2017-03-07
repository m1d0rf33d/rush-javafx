package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;
/**
 * Created by lynx on 2/21/17.
 */
public class MemberDetailsService extends BaseService{

    private ApiService apiService = App.appContextHolder.apiService;

    public void initialize() {
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
                        loadActiveVouchers();
                    } else {
                        showPrompt(apiResponse.getMessage(), "MEMBER DETAILS");
                    }
                    enableMenu();
                }
            });

            new Thread(task).start();
        });
        pause.play();
    }

    public void viewMember(String mobileNumber) {
        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(1)
        );
        pause.setOnFinished(event -> {
            Task task = viewMemberWorker(mobileNumber);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        App.appContextHolder.routeService.loadMemberDetailsScreen();
                    } else {
                        showPrompt(apiResponse.getMessage(), "MEMBER INQUIRY");
                        enableMenu();
                    }

                }
            });

            new Thread(task).start();
        });
        pause.play();
    }

    public Task viewMemberWorker(String mobileNumber) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {

                return loginCustomer(mobileNumber);
            }
        };
    }

    public Task initializeWorker() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                Customer customer = App.appContextHolder.getCustomer();
                ApiResponse loginResponse = loginCustomer(customer.getMobileNumber());
                if (loginResponse.isSuccess()) {
                    getActiveVouchers();
                    apiResponse.setSuccess(true);
                } else {
                    showPrompt(apiResponse.getMessage(), "MEMBER DETAILS");
                }

                return apiResponse;
            }
        };
    }

    public ApiResponse loginCustomer(String mobileNumber) {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        JSONObject payload = new JSONObject();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("mobile_no", mobileNumber));
        String url = BASE_URL + MEMBER_LOGIN_ENDPOINT;

        Employee employee = App.appContextHolder.getEmployee();
        url = url.replace(":employee_id", employee.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                Customer customer = new Customer();
                customer.setMobileNumber((String) data.get("mobile_no"));
                customer.setGender((String) data.get("gender"));
                customer.setMemberId((String) data.get("profile_id"));
                customer.setName((String) data.get("name"));
                customer.setDateOfBirth((String) data.get("birthdate"));
                customer.setEmail((String) data.get("email"));
                customer.setMemberSince((String) data.get("registration_date"));

                customer.setUuid((String) data.get("id"));
                customer.setMobileNumber((String) data.get("mobile_no"));

                url = BASE_URL + GET_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid", customer.getUuid());
                jsonObject = apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
                String points = (String) jsonObject.get("data");
                customer.setAvailablePoints(points);

                App.appContextHolder.setCustomer(customer);
                apiResponse.setSuccess(true);
                apiResponse.setMessage("Redeem reward successful.");
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }

        }else {
            apiResponse.setMessage("Network error.");
        }

        apiResponse.setPayload(payload);
        return apiResponse;
    }

    public CustomerCard getCustomerCard() {

        Employee employee = App.appContextHolder.getEmployee();
        Customer customer = App.appContextHolder.getCustomer();

        List<NameValuePair> params = new ArrayList<>();
        String url = BASE_URL + CUSTOMER_CARD_ENDPOINT;
        url = url.replace(":employee_id", employee.getEmployeeId()).replace(":customer_id", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            CustomerCard card = new CustomerCard();

            JSONObject data = (JSONObject) jsonObject.get("data");
            JSONObject promoJSON = (JSONObject) data.get("promo");

            Promo promo = new Promo();
            List<Reward> rewards = new ArrayList<>();
            List<JSONObject> rewardsJSON = (ArrayList) promoJSON.get("rewards");
            for (JSONObject rewardJSON : rewardsJSON) {
                Reward reward = new Reward();
                reward.setId((String) rewardJSON.get("id"));
                reward.setImageUrl((String) rewardJSON.get("image_url"));
                reward.setDetails((String) rewardJSON.get("details"));
                reward.setName((String) rewardJSON.get("name"));
                reward.setStamps(((Long) rewardJSON.get("stamps")).intValue());
                rewards.add(reward);
            }
            if (data.get("stamps") != null) {
                promo.setStamps(((Long) data.get("stamps")).intValue());
            }
            promo.setRewards(rewards);
            card.setPromo(promo);
            return card;
        }
        return null;
    }


    public ApiResponse getActiveVouchers() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        Customer customer = App.appContextHolder.getCustomer();

        String url = BASE_URL + CUSTOMER_REWARDS_ENDPOINT;
        url = url.replace(":id", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, new ArrayList<>(), "get", CUSTOMER_APP_RESOUCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                List<JSONObject> dataJSON = (ArrayList) jsonObject.get("data");
                List<Reward> rewards = new ArrayList<>();
                for (JSONObject rewardJSON : dataJSON) {
                    Reward reward = new Reward();
                    reward.setDetails((String) rewardJSON.get("details"));
                    reward.setDate((String) rewardJSON.get("date"));
                    reward.setName((String) rewardJSON.get("name"));/*
                    reward.setQuantity((rewardJSON.get("quantity")).toString());*/
                    reward.setId(((Long)rewardJSON.get("id")).toString());
                    reward.setImageUrl((String) rewardJSON.get("image_url"));
                    reward.setPointsRequired(String.valueOf(rewardJSON.get("points")));
                    rewards.add(reward);
                }
                customer.setActiveVouchers(rewards);
                apiResponse.setSuccess(true);
            }
        }
        return apiResponse;
    }



    private void loadActiveVouchers() {


        VBox rootVBox = App.appContextHolder.getRootContainer();
        Pagination activeVouchersPagination = (Pagination) rootVBox.getScene().lookup("#activeVouchersPagination");

        activeVouchersPagination.setPageCount(0);
        activeVouchersPagination.setPageFactory((Integer pageIndex) -> createActivateVoucherPage(pageIndex));
    }

    public Node createActivateVoucherPage(int pageIndex) {
        int pageCount = 0;
        int maxEntries = 10;

        Customer customer = App.appContextHolder.getCustomer();
        pageCount = customer.getActiveVouchers().size() / maxEntries;
        if (pageCount == 0) {
            pageCount = 1;
        }

        VBox box = new VBox();
        box.getChildren().addAll(buildTableView());
        return box;
    }

    private TableView<Reward> buildTableView() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField searchTextField = (TextField) rootVBox.getScene().lookup("#searchTextField");
        Pagination activeVouchersPagination = (Pagination) rootVBox.getScene().lookup("#activeVouchersPagination");

        TableView<Reward> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<Reward> finalData = FXCollections.observableArrayList();
        ObservableList<Reward> textFilteredData = FXCollections.observableArrayList();
        Customer customer = App.appContextHolder.getCustomer();
        List<Reward> rewards = customer.getActiveVouchers();
        if (rewards != null) {
            if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
                String searchTxt = searchTextField.getText().toLowerCase();
                for (Reward reward : rewards) {
                    if (reward.getDetails().toLowerCase().contains(searchTxt)
                            || reward.getName().toLowerCase().contains(searchTxt)
                            || reward.getQuantity().toLowerCase().contains(searchTxt)) {
                        textFilteredData.add(reward);
                    }
                }
            } else {
                textFilteredData.addAll(rewards);
            }
            int maxEntries = 10;
            int pageCount = textFilteredData.size() / maxEntries;
            if (pageCount == 0) {
                pageCount = 1;
            } else {
                if (textFilteredData.size() % maxEntries > 0) {
                    pageCount++;
                }
            }

            activeVouchersPagination.setPageCount(pageCount);
            int pageIndex = activeVouchersPagination.getCurrentPageIndex();
            ObservableList<Reward> indexFilteredData = FXCollections.observableArrayList();
            for (Reward reward : textFilteredData) {
                int objIndex = textFilteredData.indexOf(reward);
                if (objIndex >= (pageIndex * maxEntries)  && objIndex < ((pageIndex + 1) * maxEntries)) {
                    indexFilteredData.add(reward);
                }
                if (objIndex > ((pageIndex + 1) * maxEntries -1)) {
                    break;
                }
            }
            finalData = indexFilteredData;
        }

        buildActiveVouchersColumns(transactionsTableView);
        transactionsTableView.setItems(finalData);
        return transactionsTableView;
    }

    private void buildActiveVouchersColumns(TableView tableView) {


        TableColumn dateCol = new TableColumn("Redemption Date");
        dateCol.setPrefWidth(200);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<>("date"));


        TableColumn rewardCol = new TableColumn("Reward");
        rewardCol.setPrefWidth(200);
        rewardCol.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        TableColumn mobileCol = new TableColumn("Details");
        mobileCol.setPrefWidth(400);
        mobileCol.setCellValueFactory(
                new PropertyValueFactory<>("details"));

        TableColumn orCol = new TableColumn("Quantity");
        orCol.setPrefWidth(100);
        orCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));


        tableView.getColumns().clear();
        tableView.getColumns().addAll(dateCol,orCol, rewardCol, mobileCol);
    }

}
