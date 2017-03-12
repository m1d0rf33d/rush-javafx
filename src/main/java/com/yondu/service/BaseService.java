package com.yondu.service;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.constants.AppConfigConstants;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;

/**
 * Created by erwin on 3/1/2017.
 */
public class BaseService {

    public void enableMenu() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        rootVBox.setOpacity(1);
        for (Node n :  rootVBox.getChildren()) {
            n.setDisable(false);
        }
    }

    public void disableMenu() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        rootVBox.setOpacity(.50);
        for (Node n :  rootVBox.getChildren()) {
            n.setDisable(true);
        }
    }

    public void showPrompt(String message, String header) {
        Label label = new Label(message);
        label.setPadding(new Insets(10,0,0,0));
        label.setFont(new Font(15.0));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle(AppConfigConstants.APP_TITLE);
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
        alert.setHeaderText(header);
        alert.getDialogPane().setPadding(new Insets(10,10,10,10));
        alert.getDialogPane().setContent(label);
        alert.getDialogPane().setPrefWidth(400);
        alert.show();
    }

    public void loadCustomerDetails() {
        VBox rootVBox = App.appContextHolder.getRootContainer();
        Label nameLabel = (Label) rootVBox.getScene().lookup("#nameLabel");
        Label memberIdLabel = (Label) rootVBox.getScene().lookup("#memberIdLabel");
        Label mobileNumberLabel = (Label) rootVBox.getScene().lookup("#mobileNumberLabel");
        Label membershipDateLabel = (Label) rootVBox.getScene().lookup("#membershipDateLabel");
        Label genderLabel = (Label) rootVBox.getScene().lookup("#genderLabel");
        Label birthdateLabel = (Label) rootVBox.getScene().lookup("#birthdateLabel");
        Label emailLabel = (Label) rootVBox.getScene().lookup("#emailLabel");
        Label pointsLabel = (Label) rootVBox.getScene().lookup("#pointsLabel");

        Customer customer = App.appContextHolder.getCustomer();
        nameLabel.setText(customer.getName());
        memberIdLabel.setText(customer.getMemberId());
        mobileNumberLabel.setText(customer.getMobileNumber());
        membershipDateLabel.setText(customer.getMemberSince());
        genderLabel.setText(customer.getGender());
        birthdateLabel.setText(customer.getDateOfBirth());
        emailLabel.setText(customer.getEmail());
        pointsLabel.setText(customer.getAvailablePoints());
    }
}
