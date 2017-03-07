package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.service.RegisterService;
import com.yondu.utils.PropertyBinder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

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
    @FXML
    public Button clearButton;

    private ToggleGroup toggleGroup = new ToggleGroup();
    private RegisterService registerService = App.appContextHolder.registerService;

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

        //Event handlers for clickable nodes
        birthdatePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                FXVK.detach();

            }

        });

        registerButton.setOnMouseClicked((MouseEvent e) -> {
            Customer customer = new Customer();
            customer.setName(nameTextField.getText());
            customer.setEmail(emailTextField.getText());
            customer.setMobileNumber(mobileTextField.getText());
            customer.setMpin(mpinTextField.getText());
            customer.setBirthdate(birthdatePicker.getValue());
            String gender = null;

            Toggle selectedToggle = toggleGroup.getSelectedToggle();
            if (selectedToggle != null) {
                gender = selectedToggle.getUserData().toString();
                customer.setGender(gender);
            }
            registerService.register(customer, toggleGroup);
        });

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            registerService.clearFields(toggleGroup);
        });
        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindMaxLength(11, mobileTextField);
        PropertyBinder.bindNumberOnly(mpinTextField);
        PropertyBinder.bindMaxLength(4, mpinTextField);
    }

}
