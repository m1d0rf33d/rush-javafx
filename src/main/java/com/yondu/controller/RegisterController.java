package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.RegisterService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
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
    private RegisterService registerService = new RegisterService();

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

        registerButton.setOnMouseClicked((MouseEvent e) -> {

            disableMenu();
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.5)
            );
            pause.setOnFinished(event -> {
                String name = nameTextField.getText();
                String email = emailTextField.getText();
                String mobile = mobileTextField.getText();
                String mpin = mpinTextField.getText();
                LocalDate birthDate = birthdatePicker.getValue();
                String gender = null;

                Toggle selectedToggle = toggleGroup.getSelectedToggle();
                if (selectedToggle != null) {
                    gender = selectedToggle.getUserData().toString();
                }

                ApiResponse apiResponse = registerService.register(name, email, mobile, mpin, birthDate, gender);

                if (apiResponse.isSuccess()) {
                    clearFields();
                }
                notifyRegistrationResult(apiResponse.getMessage(), nameTextField.getScene().getWindow());
                enableMenu();
            });
            pause.play();

        });

        clearButton.setOnMouseClicked((MouseEvent e) -> {
            clearFields();
        });
        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindMaxLength(11, mobileTextField);
        PropertyBinder.bindNumberOnly(mpinTextField);
        PropertyBinder.bindMaxLength(4, mpinTextField);
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

    public void disableMenu() {
        App.appContextHolder.getRootVBox().setOpacity(.50);
        for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
            n.setDisable(true);
        }
    }
    public void enableMenu() {
        App.appContextHolder.getRootVBox().setOpacity(1);
        for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
            n.setDisable(false);
        }
    }
}
