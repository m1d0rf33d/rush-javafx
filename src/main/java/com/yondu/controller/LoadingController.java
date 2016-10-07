package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by erwin on 10/2/2016.
 */
public class LoadingController implements Initializable{

    @FXML
    public ImageView rushLogoImage;

    private ApiService apiService;

    private String orStr;
    private String totalAmountStr;
    private String convertedPoints;
    private Account customer;
    private String baseUrl;

    private String pointsConversionEndpoint;
    private String memberLoginEndpoint;
    private String getPointsEndpoint;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService();
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            prop.load(inputStream);
            inputStream.close();
            baseUrl = prop.getProperty("base_url");
            pointsConversionEndpoint = prop.getProperty("points_conversion_endpoint");
            memberLoginEndpoint = prop.getProperty("member_login_endpoint");
            getPointsEndpoint = prop.getProperty("get_points_endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Prepare data
        SalesCaptureService myService = new SalesCaptureService();
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                OrCaptureService orCaptureService = new OrCaptureService();
                orCaptureService.setOnSucceeded((WorkerStateEvent a) ->{
                    ConvertPointsService convertPointsService = new ConvertPointsService();
                    convertPointsService.setOnSucceeded((WorkerStateEvent t) -> {
                        CustomerInfoService customerInfoService = new CustomerInfoService();
                        customerInfoService.setOnSucceeded((WorkerStateEvent e) -> {
                            try{
                                Stage stage = new Stage();
                                FXMLLoader  loader  = new FXMLLoader(App.class.getResource(GIVE_POINTS_DETAILS_FXML));
                                PointsDetailsController pointsDetailsController = new PointsDetailsController(orStr, totalAmountStr, convertedPoints, customer);
                                loader.setController(pointsDetailsController);
                                stage.setScene(new Scene(loader.load(), 500,400));
                                stage.resizableProperty().setValue(Boolean.FALSE);
                                stage.show();

                                ((Stage)rushLogoImage.getScene().getWindow()).close();
                            }catch (Exception err){
                                err.printStackTrace();
                            }
                        });
                        customerInfoService.setOnFailed((WorkerStateEvent f) -> {
                            handleError("Total sales captured value: '" + totalAmountStr + "' is not a valid number.");
                        });
                        customerInfoService.start();
                    });
                    convertPointsService.setOnFailed((WorkerStateEvent t) -> {
                        handleError("Total sales captured value: '" + totalAmountStr + "' is not a valid number.");
                    });
                    convertPointsService.start();
                });
                orCaptureService.setOnFailed((WorkerStateEvent t) -> {
                    handleError("Or failed");
                });
                orCaptureService.start();
            }
        });
        myService.setOnFailed((WorkerStateEvent t) -> {
            handleError("Sales failed");
        });

        myService.start();

    }

    private void handleError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR,message, ButtonType.OK);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            alert.close();
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

            ((Stage)rushLogoImage.getScene().getWindow()).close();
        }
    }


    private void getTotalSales() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(System.getProperty("user.home") + AppConfigConstants.OCR_CONFIG_LOCATION));
            prop.load(inputStream);
            salesX = ((Double)Double.parseDouble(prop.getProperty("sales_pos_x"))).intValue();
            salesY =((Double) Double.parseDouble(prop.getProperty("sales_pos_y"))).intValue();
            salesWidth = ((Double)Double.parseDouble(prop.getProperty("sales_width"))).intValue();
            salesHeight = ((Double)Double.parseDouble(prop.getProperty("sales_height"))).intValue();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now that we got the screen dimensions for sales we will create an image file
        //then read the text using tesseract
        try {
            Robot robot = new Robot();
            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = tesseract.doOCR(screenFullImage);
            totalAmountStr  = outText.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOrNumber() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(System.getProperty("user.home") + AppConfigConstants.OCR_CONFIG_LOCATION));
            prop.load(inputStream);
            salesX = ((Double)Double.parseDouble(prop.getProperty("or_pos_x"))).intValue();
            salesY =((Double) Double.parseDouble(prop.getProperty("or_pos_y"))).intValue();
            salesWidth = ((Double)Double.parseDouble(prop.getProperty("or_width"))).intValue();
            salesHeight = ((Double)Double.parseDouble(prop.getProperty("or_height"))).intValue();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Now that we got the screen dimensions for sales we will create an image file
        //then read the text using tesseract
        try {
            Robot robot = new Robot();
            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            orStr  = outText.trim();

        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    public void convertPoints() throws ParseException {
        String url = baseUrl + pointsConversionEndpoint;
        String result = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = (JSONObject) parser.parse(result);
        JSONObject data = (JSONObject) jsonResponse.get("data");
        Long earningPeso = (Long) data.get("earning_peso");
        Double totalAmount = Double.parseDouble(totalAmountStr);
        Double points = totalAmount / earningPeso;
        convertedPoints = String.valueOf(points);
    }

    private class SalesCaptureService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    getTotalSales();
                    return null;
                }
            };
        }
    }

    private class OrCaptureService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    getOrNumber();
                    return null;
                }
            };
        }
    }

    private class ConvertPointsService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    convertPoints();
                    return null;
                }
            };
        }
    }

    private class CustomerInfoService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    getCustomerInformation();
                    return null;
                }
            };
        }
    }

    private void getCustomerInformation() {
        java.util.List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, App.appContextHolder.getCustomerMobile()));

        String url = baseUrl + memberLoginEndpoint.replace(":employee_id", App.appContextHolder.getEmployeeId());
        String responseStr = apiService.call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
            JSONObject data = (JSONObject) jsonResponse.get("data");
            customer = new Account();
            customer.setName((String)data.get("name"));
            customer.setMobileNumber((String) data.get("mobile_no"));
            //get customer current points
            params = new ArrayList<>();
            url = baseUrl + getPointsEndpoint.replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            responseStr = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            jsonResponse = (JSONObject) parser.parse(responseStr);
            Double points = null;
            try {
                points = (Double) jsonResponse.get("data");
            }catch (Exception e) {
                points = Double.parseDouble(String.valueOf((Long) jsonResponse.get("data")));
            }

            customer.setCurrentPoints(points);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
