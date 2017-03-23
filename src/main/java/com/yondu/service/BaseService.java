package com.yondu.service;

import com.yondu.App;
import com.yondu.model.Customer;
import com.yondu.model.TransactionType;
import com.yondu.model.constants.AppConfigConstants;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.yondu.model.constants.AppConfigConstants.*;

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
        Stage stage = (Stage) App.appContextHolder.getRootContainer().getScene().getWindow();
        stage.setIconified(false);
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

    public void saveTransaction(TransactionType transactionType,
                                String mobileNumber,
                                String employeeName,
                                String amount,
                                String orNumber,
                                String reward) {
        Task task = saveTransactionWorker(transactionType, mobileNumber, employeeName, amount, orNumber, reward);
        new Thread(task).start();
    }


    public Task saveTransactionWorker(TransactionType transactionType,
                                      String mobileNumber,
                                      String employeeName,
                                      String amount,
                                      String orNumber,
                                      String reward) {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                File file = new File(RUSH_HOME+ DIVIDER + "branchtransactions.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
                String date = df.format(new Date());

                PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
                StringBuilder sb = new StringBuilder();
                sb.append("date=" + date);
                sb.append(":");
                sb.append("transactionType=" + transactionType);
                sb.append(":");
                sb.append("employeeName=" + (employeeName != null ? employeeName : " "));
                sb.append(":");
                sb.append("mobileNumber=" + (mobileNumber != null ? mobileNumber : " "));
                sb.append(":");
                sb.append("amount=" + (amount != null ? amount : " "));
                sb.append(":");
                sb.append("orNumber=" + (orNumber != null ? orNumber : " "));
                sb.append(":");
                sb.append("reward=" + (reward != null ? reward : " "));

                byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(sb.toString().getBytes());
                fstream.println(new String(encodedBytes));
                fstream.flush();
                fstream.close();

                return null;
            }
        };
    }

    public void showLoadingScreen() {
        try {
            App.appContextHolder.loadingStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/fxml/rush-loading.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 500,300);
            scene.getStylesheets().add(App.class.getResource("/app/css/menu.css").toExternalForm());
            App.appContextHolder.loadingStage.setScene(scene);
            App.appContextHolder.loadingStage.setTitle(APP_TITLE);
            App.appContextHolder.loadingStage.initOwner(App.appContextHolder.getRootContainer().getScene().getWindow());
            App.appContextHolder.loadingStage.initStyle(StageStyle.UNDECORATED);
            App.appContextHolder.loadingStage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            App.appContextHolder.loadingStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hideLoadingScreen() {
        App.appContextHolder.loadingStage.close();
    }
}
