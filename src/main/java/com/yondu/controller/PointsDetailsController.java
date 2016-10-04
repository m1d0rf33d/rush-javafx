package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.service.ApiService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.yondu.model.constants.AppConfigConstants.*;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

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

    private ApiService apiService;

    private String orNumber;
    private String totalAmount;
    private String convertedPoints;
    private Account customer;

    private String baseUrl;
    private String givePointsEndpoint;

    public PointsDetailsController(String orNumber, String totalAmount, String convertedPoints, Account customer) {
        this.orNumber = orNumber;
        this.totalAmount = totalAmount;
        this.convertedPoints = convertedPoints;
        this.customer = customer;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = new ApiService();
        this.rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));


        this.nameLbl.setText(customer.getName());
        this.mobileNumberLbl.setText(customer.getMobileNumber());
        this.currentPointsLbl.setText(String.valueOf(customer.getCurrentPoints()));

        this.orNumberLbl.setText(this.orNumber);
        this.totalAmountLbl.setText(this.totalAmount);
        this.convertedPointsLbl.setText(this.convertedPoints);

        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            prop.load(inputStream);
            this.baseUrl = prop.getProperty("base_url");
            this.givePointsEndpoint = prop.getProperty("give_points_endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                java.util.List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumberLbl.getText().trim()));
                params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmountLbl.getText().trim()));
                String url = baseUrl + givePointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
                    if (jsonResponse.get("error_code").equals("0x0")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Give points successful", ButtonType.OK);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                            Stage givePointsStage = new Stage();
                            Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
                            givePointsStage.setScene(new Scene(root, 400,200));
                            givePointsStage.setTitle("Give Points");
                            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
                            givePointsStage.show();

                            ((Stage)rushLogoImageView.getScene().getWindow()).close();
                        }
                    } else {
                        JSONObject error = (JSONObject) jsonResponse.get("errors");
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
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        this.cancelButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Stage givePointsStage = new Stage();
                Parent root = null;
                try {
                    root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                givePointsStage.setScene(new Scene(root, 400,200));
                givePointsStage.setTitle("Give Points");
                givePointsStage.resizableProperty().setValue(Boolean.FALSE);
                givePointsStage.show();

                ((Stage)rushLogoImageView.getScene().getWindow()).close();
            }
        });
    }

}
