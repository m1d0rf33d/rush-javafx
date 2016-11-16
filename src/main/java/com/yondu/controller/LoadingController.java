package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.model.constants.ApiFieldContants;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
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

    private String orStr;
    private String totalAmountStr;
    private String convertedPoints;
    private Account customer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        //Screen shot and read total sales
        SalesCaptureService myService = new SalesCaptureService();
        myService.setOnSucceeded((WorkerStateEvent t) ->{
            //validate captured totalAmount
            try {
             /*   if (!totalAmountStr.contains(".")) {
                    Long l = Long.parseLong(totalAmountStr);
                    totalAmountStr = String.valueOf(l.doubleValue());
                }*/
                Double.parseDouble(totalAmountStr);
                //Screen shot and read or number
                OrCaptureService orCaptureService = new OrCaptureService();
                orCaptureService.setOnSucceeded((WorkerStateEvent a) ->{
                    App.appContextHolder.getLoadingStage().setIconified(false);
                    //Convert captured total sales to points
                    ConvertPointsService convertPointsService = new ConvertPointsService();
                    convertPointsService.setOnSucceeded((WorkerStateEvent c) -> {

                        //Get customer information for viewing
                        CustomerInfoService customerInfoService = new CustomerInfoService();
                        customerInfoService.setOnSucceeded((WorkerStateEvent e) -> {
                            try{
                                Stage stage = new Stage();
                                FXMLLoader  loader  = new FXMLLoader(App.class.getResource(GIVE_POINTS_DETAILS_FXML));
                                PointsDetailsController pointsDetailsController = new PointsDetailsController(orStr, totalAmountStr, convertedPoints, customer);
                                loader.setController(pointsDetailsController);
                                stage.setScene(new Scene(loader.load(), 600,500));
                                stage.resizableProperty().setValue(Boolean.FALSE);
                                stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                                stage.show();
                                App.appContextHolder.setPreviousStage("GIVE_POINTS_OCR");
                                ((Stage)rushLogoImage.getScene().getWindow()).close();
                            }catch (Exception err){
                                err.printStackTrace();
                            }
                        });
                        customerInfoService.setOnFailed((WorkerStateEvent f) -> {
                            handleError("Internal fatal error");
                        });
                        customerInfoService.start();
                    });
                    convertPointsService.setOnFailed((WorkerStateEvent x) -> {
                        handleError("Internal fatal error");
                    });
                    convertPointsService.start();
                });
                orCaptureService.setOnFailed((WorkerStateEvent d) -> {
                    handleError("OR number screen area is not configured. Please go to OCR settings and set the dimensions.");
                });
                orCaptureService.start();
            }
            catch(NumberFormatException e) {

                handleError("Captured total sales is not a valid amount : " + totalAmountStr);
            }
        });
        myService.setOnFailed((WorkerStateEvent t) -> {
            handleError("Total amount screen area is not configured. Please go to OCR settings and set the dimensions.");
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
            givePointsStage.setScene(new Scene(root, 400,220));
            givePointsStage.setTitle("Rush");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();

            ((Stage)rushLogoImage.getScene().getWindow()).close();
        }
    }


    private void getTotalSales() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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
            String basePath = "";
            if (App.appContextHolder.getIs64Bit()) {
                basePath = "C:\\Program Files (x86)";
            } else {
                basePath = "C:\\Program Files";
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(basePath + TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = tesseract.doOCR(screenFullImage);
            totalAmountStr  = outText.trim().replace(",","");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOrNumber() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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
            String basePath = "";
            if (App.appContextHolder.getIs64Bit()) {
                basePath = "C:\\Program Files (x86)";
            } else {
                basePath = "C:\\Program Files";
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(basePath + TESSERACT_LOCATION);
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
        if (App.appContextHolder.isOnlineMode()) {
           try {
               String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getPointsConversionEndpoint();
               String result = App.appContextHolder.getApiService().call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
               JSONParser parser = new JSONParser();
               JSONObject jsonResponse = (JSONObject) parser.parse(result);
               JSONObject data = (JSONObject) jsonResponse.get("data");
               Long earningPeso = (Long) data.get("earning_peso");
               Double totalAmount = Double.parseDouble(totalAmountStr);
               Double points = totalAmount / earningPeso;

               DecimalFormat formatter = new DecimalFormat("#,###.00");
               totalAmountStr = formatter.format(totalAmount);
               convertedPoints = formatter.format(points);
           } catch (IOException e) {
               e.printStackTrace();
               App.appContextHolder.setOnlineMode(false);

           }
        } else {
            convertedPoints = "0";
        }
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
                   if (App.appContextHolder.isOnlineMode()) {
                       convertPoints();
                   } else {
                       convertedPoints = "";
                   }
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
                   if (App.appContextHolder.isOnlineMode()) {
                       getCustomerInformation();
                   } else {
                       customer = new Account();
                       customer.setMobileNumber(App.appContextHolder.getCustomerMobile());
                       customer.setCurrentPoints(0d);
                       customer.setName("");
                   }
                    return null;
                }
            };
        }
    }

    private void getCustomerInformation() {

        try {
            java.util.List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.MEMBER_MOBILE, App.appContextHolder.getCustomerMobile()));

            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getMemberLoginEndpoint();
            url = url.replace(":employee_id", App.appContextHolder.getEmployeeId());
            String responseStr = App.appContextHolder.getApiService().call(url, params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseStr);
            JSONObject data = (JSONObject) jsonResponse.get("data");
            customer = new Account();
            customer.setName((String)data.get("name"));
            customer.setMobileNumber((String) data.get("mobile_no"));
            //get customer current points
            params = new ArrayList<>();
            url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetPointsEndpoint().replace(":customer_uuid",App.appContextHolder.getCustomerUUID());
            responseStr = App.appContextHolder.getApiService().call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

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
        } catch (IOException e) {
            e.printStackTrace();
            App.appContextHolder.setOnlineMode(false);
            //Alert to offline mode
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to reach server. You are now in offline mode.", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
