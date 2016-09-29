package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;

/**
 * Created by aomine on 9/29/16.
 */
public class OcrConfigController {

    private static final String SALES_CAPTURE_FXML = "/app/fxml/sales-capture.fxml";
    private Stage salesCaptureStage;

    public void loadSalesCaptureArea() {
        try {
            if (salesCaptureStage != null) {
                salesCaptureStage.close();
            }
            salesCaptureStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(SALES_CAPTURE_FXML));
            salesCaptureStage.setScene(new Scene(root, 300,100));
            salesCaptureStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void previewSalesCaptureArea() {
        try {

            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File("/home/aomine/Desktop/ocr-config.properties"));
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file api.properties not found in the classpath");
            }
            Double salesX = Double.parseDouble(prop.getProperty("sales_pos_x"));
            Double salesY = Double.parseDouble(prop.getProperty("sales_pos_y"));
            Double salesWidth = Double.parseDouble(prop.getProperty("sales_width"));
            Double salesHeight = Double.parseDouble(prop.getProperty("sales_height"));

            Robot robot = new Robot();
            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = myToolkit.getScreenSize();

            Rectangle screen = new Rectangle(salesX.intValue(),salesY.intValue(),
                                            salesWidth.intValue(), salesHeight.intValue());

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            ImageIO.write(screenFullImage, "jpg", new File("/home/aomine/Desktop/ss.jpg"));

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
