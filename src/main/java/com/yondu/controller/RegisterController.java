package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.BASE_URL;
import static com.yondu.AppContextHolder.REGISTER_ENDPOINT;

/**
 * Created by lynx on 2/7/17.
 */
public class RegisterController implements Initializable {
    @FXML
    public TextField nameTextField;
    @FXML
    public TextField emailTextField;
    @FXML
    public TextField mobileTextField;
    @FXML
    public TextField mpinTextField;
    @FXML
    public Button registerButton;
    @FXML
    public DatePicker birthdatePicker;
    @FXML
    public RadioButton maleRadioButton;
    @FXML
    public RadioButton femaleRadioButton;


    private ApiService apiService = new ApiService();
    private ToggleGroup toggleGroup = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        birthdatePicker.setEditable(true);
        maleRadioButton.setUserData("Male");
        femaleRadioButton.setUserData("Female");

        maleRadioButton.setToggleGroup(toggleGroup);
        femaleRadioButton.setToggleGroup(toggleGroup);

        birthdatePicker.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            birthdatePicker.show();
        });

        registerButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            registerCustomer();
        });
    }
    private void registerCustomer() {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_NAME, nameTextField.getText()));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_EMAIL, emailTextField.getText()));
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, mobileTextField.getText()));
        params.add(new BasicNameValuePair(ApiFieldContants.PIN, mpinTextField.getText()));

        LocalDate bdayLocalDate = birthdatePicker.getValue();
        if (bdayLocalDate != null) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/YYYY");
            String str = bdayLocalDate.format(dateTimeFormatter);
            params.add(new BasicNameValuePair(ApiFieldContants.BIRTHDATE, str));
        }
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            params.add(new BasicNameValuePair(ApiFieldContants.GENDER, selectedToggle.getUserData().toString()));
        }

        String url = BASE_URL + REGISTER_ENDPOINT;
        url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
        JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                clearFields();
                notifyRegistrationResult("Registration successful", maleRadioButton.getScene().getWindow());
            } else {
                notifyRegistrationResult((String)jsonObject.get("message"), maleRadioButton.getScene().getWindow());
            }
        } else {

        }
    }

    private void clearFields() {
        nameTextField.setText(null);
        emailTextField.setText(null);
        mobileTextField.setText(null);
        mpinTextField.setText(null);
        birthdatePicker.setValue(null);
        toggleGroup.selectToggle(null);
    }

    private void notifyRegistrationResult(String message, Window window) {
        Text text = new Text(message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(window);
        alert.setHeaderText("REGISTER MEMBER");
        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
        alert.getDialogPane().setContent(text);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }
}
