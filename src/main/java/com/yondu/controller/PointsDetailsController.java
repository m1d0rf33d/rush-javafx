package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
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
import java.text.SimpleDateFormat;
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
    @FXML
    public Label mode;

    private ApiService apiService;

    private String orNumber;
    private String totalAmount;
    private String convertedPoints;
    private Account customer;

    public PointsDetailsController(String orNumber, String totalAmount, String convertedPoints, Account customer) {
        this.orNumber = orNumber;
        this.totalAmount = totalAmount;
        this.convertedPoints = convertedPoints;
        this.customer = customer;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!App.appContextHolder.isOnlineMode()) {
            mode.setText("OFFLINE");
        }

        apiService = new ApiService();
        this.rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));


        this.nameLbl.setText(customer.getName());
        this.mobileNumberLbl.setText(customer.getMobileNumber());
        this.currentPointsLbl.setText(String.valueOf(customer.getCurrentPoints()));
        this.orNumberLbl.setText(this.orNumber);
        this.totalAmountLbl.setText(this.totalAmount);
        this.convertedPointsLbl.setText(this.convertedPoints);

        this.continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent event) ->{
            if (App.appContextHolder.isOnlineMode()) {
                try{
                    java.util.List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_UUID, App.appContextHolder.getEmployeeId()));
                    params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orNumberLbl.getText().trim()));
                    params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmountLbl.getText().trim()));
                    String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGivePointsEndpoint();
                    url = url.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
                    String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                    JSONParser parser = new JSONParser();

                    JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
                    if (jsonResponse.get("error_code").equals("0x0")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION,"Give points successful", ButtonType.OK);
                        alert.showAndWait();

                        if (alert.getResult() == ButtonType.OK) {
                            alert.close();
                            Stage givePointsStage = new Stage();
                            Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
                            givePointsStage.setScene(new Scene(root, 400,220));
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
            } else {
                //write to file
                try {
                    File file = new File(App.appContextHolder.getOfflinePath());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    SimpleDateFormat df  = new SimpleDateFormat("MM/dd/YYYY");
                    String date = df.format(new Date());

                    PrintWriter fstream = new PrintWriter(new FileWriter(file,true));
                    fstream.println("mobileNumber=" + customer.getMobileNumber()+ ",totalAmount=" + totalAmount + ", orNumber=" + orNumber + ", date=" + date);
                    fstream.flush();
                    fstream.close();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"Give points saved to offline transactions", ButtonType.OK);
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
