package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiFieldContants;
import com.yondu.service.ApiService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.yondu.model.AppConfigConstants.*;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

/**
 * Created by erwin on 10/2/2016.
 */
public class PointsDetailsController implements Initializable{

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public javafx.scene.control.Label totalAmountLabel;
    @FXML
    public Label orLabel;
    @FXML
    public Label nameLabel;
    @FXML
    public Label mobileLabel;
    @FXML
    public Button continueButton;
    @FXML
    public Button cancelButton;
    @FXML
    public Label convertedPointsLabel;

    private ApiService apiService;

    private String orNumber;
    private String totalAmount;
    private String convertedPoints;

    private String baseUrl;
    private String givePointsEndpoint;

    public PointsDetailsController(String orNumber, String totalAmount, String convertedPoints) {
        this.orNumber = orNumber;
        this.totalAmount = totalAmount;
        this.convertedPoints = convertedPoints;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = new ApiService();
        this.rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));


        this.nameLabel.setText(App.appContextHolder.getCustomerName());
        this.mobileLabel.setText(App.appContextHolder.getCustomerMobile());
        this.orLabel.setText(this.orNumber);
        this.totalAmountLabel.setText(this.totalAmount);
        this.convertedPointsLabel.setText(this.convertedPoints);

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
                params.add(new BasicNameValuePair(ApiFieldContants.OR_NUMBER, orLabel.getText().trim()));
                params.add(new BasicNameValuePair(ApiFieldContants.AMOUNT, totalAmountLabel.getText().trim()));
                String url = baseUrl + givePointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerId());
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
