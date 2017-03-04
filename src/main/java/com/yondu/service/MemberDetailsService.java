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
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private ApiService apiService = new ApiService();
    private RouteService routeService = new RouteService();

    public void initialize() {

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
            disableMenu();
            new Thread().start();
        });
        pause.play();
    }

    public void viewMember(String mobileNumber) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

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
                        routeService.loadMemberDetailsScreen();
                    } else {
                        showPrompt(apiResponse.getMessage(), "MEMBER INQUIRY");
                    }
                    enableMenu();
                }
            });
            disableMenu();
            new Thread().start();
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

    public ApiResponse getCurrentPoints() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        JSONObject payload = new JSONObject();

        Customer customer = App.appContextHolder.getCustomer();

        List<NameValuePair> params = new ArrayList<>();
        String url = BASE_URL + GET_POINTS_ENDPOINT;
        url = url.replace(":customer_uuid", customer.getUuid());
        JSONObject jsonObject = apiService.call(url, params, "get", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            payload.put("points", jsonObject.get("data"));
            apiResponse.setSuccess(true);
            apiResponse.setPayload(payload);
        } else {
            apiResponse.setMessage("Network error.");
        }
        return apiResponse;
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
                    reward.setName((String) rewardJSON.get("name"));
                    reward.setQuantity((rewardJSON.get("quantity")).toString());
                    reward.setId(String.valueOf(rewardJSON.get("id")));
                    reward.setImageUrl((String) rewardJSON.get("image_url"));
                    rewards.add(reward);
                }
                customer.setActiveVouchers(rewards);
                apiResponse.setSuccess(true);
            }
        }
        return apiResponse;
    }

    public ApiResponse loginEmployee(String employeeId, String branchId, String pin) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_id", employeeId));
        params.add(new BasicNameValuePair("branch_id", branchId));
        params.add(new BasicNameValuePair("pin", pin));
        String url = BASE_URL + LOGIN_ENDPOINT;
        JSONObject jsonObject = apiService.call((url), params, "post", MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");

                Employee employee = new Employee();
                employee.setBranchId(branchId);
                employee.setEmployeeId((String) data.get("id"));
                employee.setEmployeeName((String) data.get("name"));
                apiResponse.setSuccess(true);

                App.appContextHolder.setEmployee(employee);
            } else {
                apiResponse.setMessage((String) jsonObject.get("message"));
            }
        } else {
            apiResponse.setMessage("Network error");
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

        VBox rootVBox = App.appContextHolder.getRootContainer();
        Pagination activeVouchersPagination = (Pagination) rootVBox.getScene().lookup("#activeVouchersPagination");
        activeVouchersPagination.setPageCount(pageCount);
        return box;
    }

    private TableView<Reward> buildTableView() {


        TableView<Reward> transactionsTableView = new TableView();
        transactionsTableView.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        ObservableList<Reward> textFilteredData = FXCollections.observableArrayList();
/*

        if (searchTextField.getText() != null && !searchTextField.getText().isEmpty()) {
            String searchTxt = searchTextField.getText().toLowerCase();
            for (Reward reward : masterData) {
                if (reward.getDetails().toLowerCase().contains(searchTxt)
                        || reward.getName().toLowerCase().contains(searchTxt)
                        || reward.getQuantity().toLowerCase().contains(searchTxt)) {
                    textFilteredData.addAll(reward);
                }
            }
        } else {
            textFilteredData = masterData;
        }

        PAGE_COUNT = textFilteredData.size() / MAX_ENTRIES_COUNT;
        if (PAGE_COUNT == 0) {
            PAGE_COUNT = 1;
        }

        int pageIndex = pagination.getCurrentPageIndex();
        ObservableList<Reward> indexFilteredData = FXCollections.observableArrayList();
        for (Reward reward : textFilteredData) {
            int objIndex = textFilteredData.indexOf(reward);
            if (objIndex >= (pageIndex * MAX_ENTRIES_COUNT)  && objIndex < ((pageIndex + 1) * MAX_ENTRIES_COUNT)) {
                indexFilteredData.add(reward);
            }
            if (objIndex > ((pageIndex + 1) * MAX_ENTRIES_COUNT -1)) {
                break;
            }
        }
*/
        Customer customer = App.appContextHolder.getCustomer();

        buildActiveVouchersColumns(transactionsTableView);
        transactionsTableView.setItems(FXCollections.observableArrayList(customer.getActiveVouchers()));
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
