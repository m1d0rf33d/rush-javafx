package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import com.yondu.service.RouteService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;
import static com.yondu.AppContextHolder.*;

/**
 * Created by erwin on 10/2/2016.
 */
public class PointsDetailsController implements Initializable{

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public Label orNumberLbl;
    @FXML
    public Label nameLbl;
    @FXML
    public Label mobileNumberLbl;
    @FXML
    public Button continueButton;
    @FXML
    public Button cancelButton;
    @FXML
    public Label convertedPointsLbl;
    @FXML
    public Label totalAmountLbl;
    @FXML
    public Label currentPointsLbl;
    @FXML
    public Label mode;

    private RouteService routeService = new RouteService();
    private ApiService apiService = new ApiService();

    private String orNumber;
    private String totalAmount;
    private String convertedPoints;
    private Account customer;
    private Stage currentStage;


    public PointsDetailsController(String orNumber, String totalAmount, String convertedPoints, Account customer) {
        this.orNumber = orNumber;
        this.totalAmount = totalAmount;
        this.convertedPoints = convertedPoints;
        this.customer = customer;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentStage = (Stage) continueButton.getScene().getWindow();
        this.rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));


        this.nameLbl.setText(customer.getName());
        this.mobileNumberLbl.setText(customer.getMobileNumber());
        this.currentPointsLbl.setText(String.valueOf(customer.getCurrentPoints()));
        this.orNumberLbl.setText(this.orNumber);
        this.totalAmountLbl.setText(this.totalAmount);
        this.convertedPointsLbl.setText(this.convertedPoints);

        if (!App.appContextHolder.isOnlineMode()) {
            mode.setText("OFFLINE");
            currentPointsLbl.setText("");
        }

        this.continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent event) ->{
            if (App.appContextHolder.isOnlineMode()) {
                java.util.List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumberLbl.getText().trim()));
                params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmountLbl.getText().trim().replace(",","")));
                String url = BASE_URL + GIVE_POINTS_ENDPOINT;
                url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                JSONObject jsonObject = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                if (jsonObject != null) {
                    if (jsonObject.get("error_code").equals("0x0")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Points has been successfully given to customer.", ButtonType.OK);
                        alert.setTitle(AppConfigConstants.APP_TITLE);
                        alert.initStyle(StageStyle.UTILITY);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                            routeService.goToGivePointsScreen(currentStage);
                        }
                    } else if (jsonObject.get("error_code").equals("0x4") || jsonObject.get("error_code").equals("0x3")) {
                        String errorMessage = (String) jsonObject.get("message");
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
                        alert.setTitle(AppConfigConstants.APP_TITLE);
                        alert.initStyle(StageStyle.UTILITY);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                        }
                    } else {
                        JSONObject error = (JSONObject) jsonObject.get("errors");
                        String errorMessage = "";
                        if (error.get("or_no") != null) {
                            List<String> l = (ArrayList<String>) error.get("or_no");
                            errorMessage = l.get(0);
                        }
                        if (error.get("amount") != null) {
                            List<String> l = (ArrayList<String>) error.get("amount");
                            errorMessage = l.get(0);
                        }
                        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
                        alert.setTitle(AppConfigConstants.APP_TITLE);
                        alert.initStyle(StageStyle.UTILITY);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                        }
                    }
                } else {
                    writeOfflineTransaction();
                }
            } else {
                writeOfflineTransaction();
            }
        });

        this.cancelButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (App.appContextHolder.getPreviousStage().equals("GIVE_POINTS_OCR")) {
                    routeService.goToGivePointsScreen(currentStage);
                } else {
                    routeService.goToGivePointsManualScreen(currentStage);
                }
            }
        });
    }
    private  void writeOfflineTransaction() {
        //write to file
        try {
            File file = new File(System.getenv("RUSH_HOME") + DIVIDER + OFFLINE_TRANSACTION_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
            String date = df.format(new Date());

            PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
            String line = "mobileNumber=" + customer.getMobileNumber().replace(":", "")+ ":totalAmount=" + totalAmount.replace(":", "") + ":orNumber=" + orNumber.replace(":", "") + ":date=" + date;
            byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(line.getBytes());
            fstream.println(new String(encodedBytes));
            fstream.flush();
            fstream.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION,"Give points data has been recorded, you may view this on offline transactions.", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                alert.close();
                if (App.appContextHolder.getPreviousStage().equals("GIVE_POINTS_OCR")) {
                    routeService.goToGivePointsScreen(currentStage);
                } else {
                    routeService.goToGivePointsManualScreen(currentStage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
