package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.Employee;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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

        disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = registerWorker(customer);
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        clearFields(toggleGroup);
                    }
                    showPrompt(apiResponse.getMessage(), "REGISTER");
                    enableMenu();
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

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("name", customer.getName()));
                params.add(new BasicNameValuePair("email", customer.getEmail()));
                params.add(new BasicNameValuePair("mobile_no", customer.getMobileNumber()));
                params.add(new BasicNameValuePair("pin", customer.getMpin()));

                if (customer.getBirthdate() != null) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
                    String str = customer.getBirthdate().format(dateTimeFormatter);
                    params.add(new BasicNameValuePair("birthdate", str));
                }

                if (customer.getGender() != null) {
                    params.add(new BasicNameValuePair("gender", customer.getGender()));
                }

                Employee employee = App.appContextHolder.getEmployee();
                String url = BASE_URL + REGISTER_ENDPOINT;
                url = url.replace(":employee_id", employee.getEmployeeId());
                JSONObject jsonObject = apiService.call(url, params, "post", MERCHANT_APP_RESOURCE_OWNER);

                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        apiResponse.setSuccess(true);
                        apiResponse.setMessage("Registration successful");
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

    public void clearFields(ToggleGroup toggleGroup) {

        VBox rootVBox = App.appContextHolder.getRootContainer();
        TextField nameTextField = (TextField) rootVBox.getScene().lookup("#nameTextField");
        TextField emailTextField = (TextField) rootVBox.getScene().lookup("#emailTextField");
        TextField mobileTextField = (TextField) rootVBox.getScene().lookup("#mobileTextField");
        TextField mpinTextField = (TextField) rootVBox.getScene().lookup("#mpinTextField");
        DatePicker birthdatePicker = (DatePicker) rootVBox.getScene().lookup("#birthdatePicker");

        nameTextField.setText(null);
        emailTextField.setText(null);
        mobileTextField.setText(null);
        mpinTextField.setText(null);
        birthdatePicker.setValue(null);
        toggleGroup.selectToggle(null);
    }

}
