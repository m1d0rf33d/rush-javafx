package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.image.Image;
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

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
import static org.junit.Assert.assertTrue;

/**
 * Created by aomine on 9/29/16.
 */
public class SalesPreviewController implements Initializable {

    @FXML
    public ImageView previewImage;
    @FXML
    public TextArea salesPreviewText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        if (App.appContextHolder.getSalesPosX() != null) {
            //Create image based from temporary ocr config
            salesX = App.appContextHolder.getSalesPosX();
            salesY = App.appContextHolder.getSalesPosY();
            salesWidth = App.appContextHolder.getSalesWidth();
            salesHeight = App.appContextHolder.getSalesHeight();
        } else {
            try {
            //Create from ocr-config.properties
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(new File("/home/aomine/Desktop/ocr.properties"));
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
        try {
            Robot robot = new Robot();

            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = myToolkit.getScreenSize();

            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            File imageFile = new File("C:\\Users\\erwin\\Desktop\\ocr.properties");
            ImageIO.write(screenFullImage, "jpg", imageFile);
            this.previewImage.setImage(new Image(new FileInputStream(imageFile)));

            BytePointer outText;
            tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
            // Initialize tesseract-ocr with English, without specifying tessdata path
            if (api.Init("C:\\Program Files (x86)\\Tesseract-OCR", "eng") != 0) {
                System.err.println("Could not initialize tesseract.");
                System.exit(1);
            }


            // Open input image with leptonica library
            lept.PIX image = pixRead("C:\\Users\\erwin\\Desktop\\ocr.properties");
            api.SetImage(image);
            // Get OCR result
            outText = api.GetUTF8Text();
            String string = outText.getString();

            this.salesPreviewText.setText(string);
            // Destroy used object and release memory
            api.End();
            outText.deallocate();
            pixDestroy(image);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
