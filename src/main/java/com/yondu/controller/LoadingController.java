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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.model.AppConfigConstants.*;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

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
    private String baseUrl;

    private String pointsConversionEndpoint;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService();
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        //Prepare data
        MyService myService = new MyService();


        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

                try {
                    Stage stage = new Stage();
                    FXMLLoader  loader  = new FXMLLoader(App.class.getResource(GIVE_POINTS_DETAILS_FXML));
                    PointsDetailsController pointsDetailsController = new PointsDetailsController(orStr, totalAmountStr, convertedPoints);
                    loader.setController(pointsDetailsController);
                    stage.setScene(new Scene(loader.load(), 500,350));
                    stage.resizableProperty().setValue(Boolean.FALSE);
                    stage.show();

                    ((Stage)rushLogoImage.getScene().getWindow()).close();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        myService.start();

    }


    private void getTotalSales() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
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
            ITesseract tesseract = new Tesseract1();
            tesseract.setDatapath(TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = tesseract.doOCR(screenFullImage);
            totalAmountStr  = outText;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOrNumber() {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
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
            ITesseract tesseract = new Tesseract1();
            tesseract.setDatapath(TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            orStr  = outText;

        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    public void convertPoints() {
        String url = baseUrl + pointsConversionEndpoint;
        String result = apiService.call(url, new ArrayList<>(), "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonResponse = (JSONObject) parser.parse(result);
            JSONObject data = (JSONObject) jsonResponse.get("data");
            Long earningPeso = (Long) data.get("earning_peso");
            Double totalAmount = Double.parseDouble(totalAmountStr);
            Double points = totalAmount / earningPeso;
            convertedPoints = String.valueOf(points);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class MyService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        Properties prop = new Properties();
                        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
                        prop.load(inputStream);
                        inputStream.close();
                        baseUrl = prop.getProperty("base_url");
                        pointsConversionEndpoint = prop.getProperty("points_conversion_endpoint");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    getTotalSales();
                    getOrNumber();
                    convertPoints();
                    return null;
                }
            };
        }
    }
}
