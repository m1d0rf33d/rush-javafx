package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiFieldContants;
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

    private ApiService apiService;
    private String baseUrl;
    private String givePointsEndpoint;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        apiService = new ApiService();
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
            prop.load(inputStream);
            this.baseUrl = prop.getProperty("base_url");
            this.givePointsEndpoint = prop.getProperty("give_points_endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        this.getTotalSales();
        this.getOrNumber();
        this.nameLabel.setText(App.appContextHolder.getCustomerName());
        this.mobileLabel.setText(App.appContextHolder.getCustomerMobile());

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
                        Alert alert = new Alert(Alert.AlertType.ERROR,(String) jsonResponse.get("message"), ButtonType.OK);
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
            File imageFile = new File(CAPTURE_IMAGE_FILE);
            ImageIO.write(screenFullImage, "jpg", imageFile);

            BytePointer outText;
            tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
            // Initialize tesseract-ocr with English, without specifying tessdata path
            if (api.Init(TESSERACT_LOCATION, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            // Open input image with leptonica library
            lept.PIX image = pixRead(CAPTURE_IMAGE_FILE);
            api.SetImage(image);
            // Get OCR result
            outText = api.GetUTF8Text();
            String string = outText.getString();
            totalAmountLabel.setText(string);
           // this.previewText.setText(string);
            // Destroy used object and release memory
            api.End();
            outText.deallocate();
            pixDestroy(image);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
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
            File imageFile = new File(CAPTURE_IMAGE_FILE);
            ImageIO.write(screenFullImage, "jpg", imageFile);

            BytePointer outText;
            tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
            // Initialize tesseract-ocr with English, without specifying tessdata path
            if (api.Init(TESSERACT_LOCATION, "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }
            // Open input image with leptonica library
            lept.PIX image = pixRead(CAPTURE_IMAGE_FILE);
            api.SetImage(image);
            // Get OCR result
            outText = api.GetUTF8Text();
            String string = outText.getString();
            orLabel.setText(string);
            // this.previewText.setText(string);
            // Destroy used object and release memory
            api.End();
            outText.deallocate();
            pixDestroy(image);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }



}
