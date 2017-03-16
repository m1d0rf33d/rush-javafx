package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.javafx.scene.control.skin.LabeledImpl;
import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.Merchant;
import com.yondu.model.Title;
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
 * Created by aomine on 3/16/17.
 */
public class SgRegisterController implements Initializable{

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
    @FXML
    public ComboBox titleComboBox;
    @FXML
    public TextField accountNumberTextField;

    private ToggleGroup toggleGroup = new ToggleGroup();
    private RegisterService registerService = App.appContextHolder.registerService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertyBinder.bindLettersOnly(nameTextField);
        PropertyBinder.bindVirtualKeyboard(nameTextField);
        PropertyBinder.bindVirtualKeyboard(emailTextField);
        PropertyBinder.bindVirtualKeyboard(mobileTextField);
        PropertyBinder.bindVirtualKeyboard(mpinTextField);
        PropertyBinder.bindVirtualKeyboard(accountNumberTextField);
        PropertyBinder.bindNumberOnly(accountNumberTextField);

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
            customer.setAccountNumber(accountNumberTextField.getText());

            String selectedTitle = (String) titleComboBox.getSelectionModel().getSelectedItem();

            if (selectedTitle != null && (!selectedTitle.isEmpty())) {
                Merchant merchant = App.appContextHolder.getMerchant();
                for (Title title : merchant.getTitles()) {
                    if (title.getName().equals(selectedTitle)) {
                        customer.setTitle(title);
                    }
                }
            }

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

        Merchant merchant = App.appContextHolder.getMerchant();
        if (merchant.getTitles() != null) {
            for (Title title : merchant.getTitles()) {
                titleComboBox.getItems().add(title.getName());

            }
        }
    }
}
