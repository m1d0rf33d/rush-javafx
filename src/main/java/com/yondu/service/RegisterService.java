package com.yondu.service;

import com.yondu.App;
import com.yondu.model.*;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.yondu.model.constants.ApiConstants.*;

/**
 * Created by lynx on 2/21/17.
 */
public class RegisterService extends BaseService {

    private ApiService apiService = App.appContextHolder.apiService;

    public void register(Customer customer, ToggleGroup toggleGroup) {
        showLoadingScreen();
        disableMenu();
        App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.WAIT);
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            Task task = registerWorker(customer);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    boolean success = false;
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        clearFields(toggleGroup);
                        success = true;
                        Employee employee = App.appContextHolder.getEmployee();
                        saveTransaction(TransactionType.REGISTER, customer.getMobileNumber(), employee.getEmployeeName(), null, null, null);
                    }

                    hideLoadingScreen();
                    showPrompt(apiResponse.getMessage(), "REGISTER");
                    enableMenu();
                    App.appContextHolder.getRootContainer().getScene().setCursor(Cursor.DEFAULT);
                }
            });
            new Thread(task).start();

        });
        pause.play();
    }

    public Task registerWorker(Customer customer) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();

                JSONObject requestBody = new JSONObject();
                requestBody.put("name", customer.getName());
                requestBody.put("email", customer.getEmail());
                requestBody.put("mobile_no", customer.getMobileNumber());
                requestBody.put("pin", customer.getMpin());

                if (customer.getBirthdate() != null) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
                    String str = customer.getBirthdate().format(dateTimeFormatter);
                    requestBody.put("birthdate", str);
                }

                if (customer.getGender() != null) {
                    requestBody.put("gender", customer.getGender());
                }

                if (customer.getTitle() != null) {
                    requestBody.put("title_id", customer.getTitle().getId());
                }
                if (customer.getAccountNumber() != null) {
                    requestBody.put("account_number", customer.getAccountNumber());
                }

                Employee employee = App.appContextHolder.getEmployee();
                Merchant merchant = App.appContextHolder.getMerchant();

                requestBody.put("employee_id", employee.getEmployeeId());
                requestBody.put("merchant_type", merchant.getMerchantType());
                requestBody.put("merchant_key", merchant.getUniqueKey());

                String token = merchant.getToken();

                String url = CMS_URL + REGISTER_MEMBER_ENDPOINT;
                JSONObject payload = apiService.callWidget(url, requestBody.toJSONString(), "post", token);
                if (payload != null) {
                    String errorCode = (String) payload.get("error_code");
                    if (errorCode.equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Registration successful.");
                    } else {
                        apiResponse.setMessage((String) payload.get("message"));
                    }
                } else {
                    apiResponse.setMessage("Network connection error.");
                }

                return apiResponse;
            }
        };
    }

    public void clearFields(ToggleGroup toggleGroup) {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField nameTextField = (TextField) rootVBox.getScene().lookup("#nameTextField");
        TextField emailTextField = (TextField) rootVBox.getScene().lookup("#emailTextField");
        TextField mobileTextField = (TextField) rootVBox.getScene().lookup("#mobileTextField");
        TextField mpinTextField = (TextField) rootVBox.getScene().lookup("#mpinTextField");
        DatePicker birthdatePicker = (DatePicker) rootVBox.getScene().lookup("#birthdatePicker");

        Merchant merchant = App.appContextHolder.getMerchant();
        if (!merchant.getMerchantClassification().equals("BASIC")) {
            TextField accountNumberTextField = (TextField) rootVBox.getScene().lookup("#accountNumberTextField");
            ComboBox titleComboBox = (ComboBox) rootVBox.getScene().lookup("#titleComboBox");
            titleComboBox.getSelectionModel().select(null);
            accountNumberTextField.setText(null);
        }

        nameTextField.setText(null);
        emailTextField.setText(null);
        mobileTextField.setText(null);
        mpinTextField.setText(null);
        birthdatePicker.setValue(null);
        toggleGroup.selectToggle(null);
    }

}
