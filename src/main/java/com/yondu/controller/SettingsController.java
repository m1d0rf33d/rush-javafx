package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.model.AppConfigConstants.*;
import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;

/**
 * Created by aomine on 9/30/16.
 */
public class SettingsController implements Initializable{

    private Stage salesCaptureStage;
    private Stage orCaptureStage;

    @FXML
    public javafx.scene.control.Label salesAreaLbl;
    @FXML
    public javafx.scene.control.Label orAreaLbl;
    @FXML
    public javafx.scene.control.TextArea previewText;
    @FXML
    public ImageView previewImage;

    public void loadSalesCaptureArea() {
        try {
            if (salesCaptureStage != null) {
                salesCaptureStage.close();
            }
            salesCaptureStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(SALES_CAPTURE_FXML));
            salesCaptureStage.setScene(new Scene(root, 300,100));
            salesCaptureStage.setMaxHeight(100);
            salesCaptureStage.setMaxWidth(300);
            salesCaptureStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadOrCaptureArea() {
        try {
            if (orCaptureStage != null) {
                orCaptureStage.close();
            }
            orCaptureStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(OR_CAPTURE_FXML));
            orCaptureStage.setScene(new Scene(root, 300,100));
            orCaptureStage.setMaxHeight(100);
            orCaptureStage.setMaxWidth(300);
            orCaptureStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void previewSalesCaptureArea() {

        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        //Check if user captured temporary values if not get configuration from config file
        if (App.appContextHolder.getSalesPosX() != null) {
            //Create image based from temporary ocr config
            salesX = App.appContextHolder.getSalesPosX();
            salesY = App.appContextHolder.getSalesPosY();
            salesWidth = App.appContextHolder.getSalesWidth();
            salesHeight = App.appContextHolder.getSalesHeight();
        } else {
            try {
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
                prop.load(inputStream);
                salesX = ((Double)Double.parseDouble(prop.getProperty("sales_pos_x"))).intValue();
                salesY =((Double) Double.parseDouble(prop.getProperty("sales_pos_y"))).intValue();
                salesWidth = ((Double)Double.parseDouble(prop.getProperty("sales_width"))).intValue();
                salesHeight = ((Double)Double.parseDouble(prop.getProperty("sales_height"))).intValue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Now that we got the screen dimensions for sales we will create an image file
        //then read the text using tesseract
        try {
            Robot robot = new Robot();
            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            File imageFile = new File(CAPTURE_IMAGE_FILE);
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }
            ImageIO.write(screenFullImage, "jpg", imageFile);
            this.previewImage.setImage(new javafx.scene.image.Image(new FileInputStream(imageFile)));

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
            this.previewText.setText(string);
            // Destroy used object and release memory
            api.End();
            outText.deallocate();
            pixDestroy(image);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Load ocr-properties saved config
        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
            prop.load(inputStream);
            StringBuilder sb = new StringBuilder();
            sb.append(prop.getProperty("sales_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_width"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_height"));
            this.salesAreaLbl.setText(sb.toString());

            sb = new StringBuilder();
            sb.append(prop.getProperty("or_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("or_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("or_width"));
            sb.append(", ");
            sb.append(prop.getProperty("or_height"));
            this.orAreaLbl.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void loadNewSalesDimension() {
        if (App.appContextHolder.getSalesPosX() != null) {

            StringBuilder sb = new StringBuilder();
            sb.append(App.appContextHolder.getSalesPosX());
            sb.append(", ");
            sb.append(App.appContextHolder.getSalesPosY());
            sb.append(", ");
            sb.append(App.appContextHolder.getSalesWidth());
            sb.append(", ");
            sb.append(App.appContextHolder.getSalesHeight());
            this.salesAreaLbl.setText(sb.toString());
        }

        if (App.appContextHolder.getOrNumberPosX() != null) {

            StringBuilder sb = new StringBuilder();
            sb.append(App.appContextHolder.getOrNumberPosX());
            sb.append(", ");
            sb.append(App.appContextHolder.getOrNumberPosY());
            sb.append(", ");
            sb.append(App.appContextHolder.getOrNumberWidth());
            sb.append(", ");
            sb.append(App.appContextHolder.getOrNumberHeight());
            this.orAreaLbl.setText(sb.toString());
        }
    }

    public void resetSalesDimension() {
        //Remove temporary sales capture dimension
        App.appContextHolder.setSalesPosX(null);
        App.appContextHolder.setSalesPosY(null);
        App.appContextHolder.setSalesHeight(null);
        App.appContextHolder.setSalesWidth(null);

        App.appContextHolder.setOrNumberPosX(null);
        App.appContextHolder.setOrNumberPosY(null);
        App.appContextHolder.setOrNumberWidth(null);
        App.appContextHolder.setOrNumberHeight(null);

        //Load ocr-properties saved config
        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
            prop.load(inputStream);
            StringBuilder sb = new StringBuilder();
            sb.append(prop.getProperty("sales_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_width"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_height"));
            this.salesAreaLbl.setText(sb.toString());

            sb = new StringBuilder();
            sb.append(prop.getProperty("or_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("or_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("or_width"));
            sb.append(", ");
            sb.append(prop.getProperty("or_height"));
            this.orAreaLbl.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDimensions() {
        try {
            String salesPosX = "", salesPosY = "", salesWidth = "", salesHeight = "",
                    orPosX = "", orPosY = "", orWidth = "", orHeight = "";

            File file = new File(OCR_CONFIG_LOCATION);
            if (file.exists()) {
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(file);
                prop.load(inputStream);
                salesPosX = prop.getProperty("sales_pos_x");
                salesPosY = prop.getProperty("sales_pos_y");
                salesWidth = prop.getProperty("sales_width");
                salesHeight = prop.getProperty("sales_height");

                orPosX = prop.getProperty("orPosX");
                orPosY = prop.getProperty("orPosY");
                orWidth = prop.getProperty("orWidth");
                orHeight = prop.getProperty("orHeight");
            }
            if (!file.exists()) {
                file.createNewFile();
            }


            if (App.appContextHolder.getSalesPosX() != null) {
                //overwrite
                salesPosX = String.valueOf(App.appContextHolder.getSalesPosX());
                salesPosY = String.valueOf(App.appContextHolder.getSalesPosY());
                salesWidth = String.valueOf(App.appContextHolder.getSalesWidth());
                salesHeight = String.valueOf(App.appContextHolder.getSalesHeight());

            }
            if (App.appContextHolder.getOrNumberPosX() != null) {
                orPosX = String.valueOf(App.appContextHolder.getOrNumberPosX());
                orPosY = String.valueOf(App.appContextHolder.getOrNumberPosY());
                orWidth = String.valueOf(App.appContextHolder.getOrNumberWidth());
                orHeight = String.valueOf(App.appContextHolder.getOrNumberHeight());
            }

            //recreate file
            PrintWriter fstream = new PrintWriter(new FileWriter(file));
            fstream.println("sales_pos_x=" + salesPosX);
            fstream.println("sales_pos_y=" + salesPosY);
            fstream.println("sales_width=" + salesWidth);
            fstream.println("sales_height=" + salesHeight);
            fstream.println("or_pos_x=" + orPosX);
            fstream.println("or_pos_y=" + orPosY);
            fstream.println("or_width=" + orWidth);
            fstream.println("or_height=" + orHeight);
            fstream.flush();
            fstream.close();

            ((Stage)this.previewText.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void previewOrArea() {

        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        //Check if user captured temporary values if not get configuration from config file
        if (App.appContextHolder.getOrNumberPosX() != null) {
            //Create image based from temporary ocr config
            salesX = App.appContextHolder.getOrNumberPosX();
            salesY = App.appContextHolder.getOrNumberPosY();
            salesWidth = App.appContextHolder.getOrNumberWidth();
            salesHeight = App.appContextHolder.getOrNumberHeight();
        } else {
            try {
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(new File(OCR_CONFIG_LOCATION));
                prop.load(inputStream);
                salesX = ((Double)Double.parseDouble(prop.getProperty("or_pos_x"))).intValue();
                salesY =((Double) Double.parseDouble(prop.getProperty("or_pos_y"))).intValue();
                salesWidth = ((Double)Double.parseDouble(prop.getProperty("or_width"))).intValue();
                salesHeight = ((Double)Double.parseDouble(prop.getProperty("or_height"))).intValue();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Now that we got the screen dimensions for sales we will create an image file
        //then read the text using tesseract
        try {
            Robot robot = new Robot();
            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            File imageFile = new File(CAPTURE_IMAGE_FILE);
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }
            ImageIO.write(screenFullImage, "jpg", imageFile);
            this.previewImage.setImage(new javafx.scene.image.Image(new FileInputStream(imageFile)));

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
            this.previewText.setText(string);
            // Destroy used object and release memory
            api.End();
            outText.deallocate();
            pixDestroy(image);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void exit() {
        ((Stage)this.previewText.getScene().getWindow()).close();
    }


}
