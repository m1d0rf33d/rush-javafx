package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.AppConfigConstants;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/22/17.
 */
public class OcrService {

    public ApiResponse triggerOCR() {

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);

        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(RUSH_HOME+ DIVIDER + OCR_PROPERTIES);
            prop.load(inputStream);

            Double savedAmountPosX = prop.getProperty("sales_pos_x").isEmpty() ? null : Double.parseDouble(prop.getProperty("sales_pos_x"));
            Double savedAmountPosY = prop.getProperty("sales_pos_y").isEmpty() ? null : Double.parseDouble(prop.getProperty("sales_pos_y"));
            Double savedAmountWidth = prop.getProperty("sales_width").isEmpty() ? null : Double.parseDouble(prop.getProperty("sales_width"));
            Double savedAmountHeight = prop.getProperty("sales_height").isEmpty() ? null : Double.parseDouble(prop.getProperty("sales_height"));

            Double savedOrPosX = prop.getProperty("or_pos_x").isEmpty() ? null : Double.parseDouble(prop.getProperty("or_pos_x"));
            Double savedOrPosY = prop.getProperty("or_pos_y").isEmpty() ? null : Double.parseDouble(prop.getProperty("or_pos_y"));
            Double savedOrWidth = prop.getProperty("or_width").isEmpty() ? null : Double.parseDouble(prop.getProperty("or_width"));
            Double savedOrHeight = prop.getProperty("or_height").isEmpty() ? null : Double.parseDouble(prop.getProperty("or_height"));


            if (savedAmountPosX != null && savedOrPosX != null) {

                JSONObject payload = new JSONObject();
                payload.put("orNumber", getText(savedOrPosX, savedOrPosY, savedOrWidth, savedOrHeight).replaceAll("[^a-zA-Z0-9,.]*", ""));
                payload.put("amount", getText(savedAmountPosX, savedAmountPosY, savedAmountWidth, savedAmountHeight).replaceAll("[^,.\\d]*", ""));
                apiResponse.setPayload(payload);
                apiResponse.setSuccess(true);
            } else {
                apiResponse.setMessage("No OCR configuration found. You may fix this by going to OCR settings.");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public String getText(Double x, Double y, Double width, Double height) {
        try {
            Robot robot = new Robot();
            Rectangle screen = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(App.appContextHolder.TESSERACT_HOME);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            return outText;
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void preview(Double x, Double y, Double width, Double height, ImageView imageView, Label label) {
        if (x != null) {
            try {
                Robot robot = new Robot();
                Rectangle screen = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

                BufferedImage screenFullImage = robot.createScreenCapture(screen);
                javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);
                imageView.setImage(image);

                ITesseract tesseract = new Tesseract();
               // tesseract.setDatapath(RUSH_HOME + DIVIDER +
                tesseract.setDatapath(App.appContextHolder.TESSERACT_HOME);
                tesseract.setLanguage("eng");
                // Get OCR result
                String outText = null;
                try {
                    outText = tesseract.doOCR(screenFullImage);
                } catch (TesseractException e) {
                    e.printStackTrace();
                }
                label.setText(outText);

            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            ((Stage) App.appContextHolder.getRootVBox().getScene().getWindow()).setIconified(false);
            Text text = new Text("No position set.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
            alert.setHeaderText("OCR PREVIEW");
            alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();
        }
        ((Stage) App.appContextHolder.getRootVBox().getScene().getWindow()).setIconified(false);
        App.appContextHolder.getRootVBox().setOpacity(1);
        for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
            n.setDisable(false);
        }
    }
}
