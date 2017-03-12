package com.yondu.service;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.OcrConfig;
import com.yondu.model.constants.AppConfigConstants;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
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
public class OcrService extends BaseService{


    public void triggerOCR() {
        disableMenu();
        ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow()).setIconified(true);
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.5)
        );
        pause.setOnFinished(event -> {
            Task task = triggerOCRTask();
            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    ApiResponse apiResponse = (ApiResponse) task.getValue();
                    if (apiResponse.isSuccess()) {
                        TextField receiptTextField = (TextField) App.appContextHolder.getRootContainer().getScene().lookup("#receiptTextField");
                        TextField amountTextField = (TextField) App.appContextHolder.getRootContainer().getScene().lookup("#amountTextField");
                        JSONObject payload =  apiResponse.getPayload();
                        receiptTextField.setText((String) payload.get("orNumber"));
                        amountTextField.setText((String) payload.get("amount"));
                        ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow()).setIconified(false);

                    } else {
                        ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow()).setIconified(false);
                        showPrompt(apiResponse.getMessage(), "EARN POINTS");
                    }
                    enableMenu();
                }
            });

            new Thread(task).start();
        });
        pause.play();

    }

    public Task triggerOCRTask() {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
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
        };
    }

    public String getText(Double x, Double y, Double width, Double height) {
        try {
            Robot robot = new Robot();
            Rectangle screen = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(TESSERACT_HOME);
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

    public void preview(OcrConfig config, String type) {
       // disableMenu();
        PauseTransition pause = new PauseTransition(
                Duration.seconds(.01)
        );
        pause.setOnFinished(event -> {
            ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow()).setIconified(true);
            PauseTransition p = new PauseTransition(
                    Duration.seconds(.50)
            );
            p.setOnFinished(ev -> {
                Task task = previewWorker(config, type);
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        ApiResponse apiResponse = (ApiResponse) task.getValue();
                        if (apiResponse.isSuccess()) {

                            VBox rootVBox = App.appContextHolder.getRootContainer();
                            ImageView previewImageView = (ImageView) rootVBox.getScene().lookup("#previewImageView");
                            Label previewLabel = (Label) rootVBox.getScene().lookup("#previewLabel");

                            JSONObject payload = apiResponse.getPayload();
                            javafx.scene.image.Image image = (Image) payload.get("image");
                            previewLabel.setText((String) payload.get("text"));
                            previewImageView.setImage(image);
                        } else {
                            showPrompt(apiResponse.getMessage(), "OCR");
                        }
                        ((Stage) App.appContextHolder.getRootContainer().getScene().getWindow()).setIconified(false);
                        enableMenu();
                    }
                });
                new Thread(task).start();

            });
            p.play();

        });
        pause.play();


    }

    public Task previewWorker(OcrConfig config, String type) {
        return new Task() {
            @Override
            protected ApiResponse call() throws Exception {
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.setSuccess(false);

                int x = 0, y = 0, width = 0, height = 0;
                if (type.equals("sales")) {
                    if (config.getSalesPosX() == null) {
                        apiResponse.setMessage("No OCR configuration found.");
                        return apiResponse;
                    } else {
                        x = config.getSalesPosX();
                        y = config.getSalesPosY();
                        width = config.getSalesWidth();
                        height = config.getSalesHeight();
                    }
                }

                if (type.equals("or")) {
                    if (config.getOrNumberPosX() == null) {
                        apiResponse.setMessage("No OCR configuration found.");
                        return apiResponse;
                    } else {
                        x = config.getOrNumberPosX();
                        y = config.getOrNumberPosY();
                        width = config.getOrNumberWidth();
                        height = config.getOrNumberHeight();
                    }
                }

                try {
                    Robot robot = new Robot();
                    Rectangle screen = new Rectangle(x, y, width, height);

                    BufferedImage screenFullImage = robot.createScreenCapture(screen);
                    javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);
                    String xs = TESSERACT_HOME;
                    ITesseract tesseract = new Tesseract();
                    tesseract.setDatapath(TESSERACT_HOME);
                    tesseract.setLanguage("eng");
                    // Get OCR result
                    String outText = null;
                    try {
                        outText = tesseract.doOCR(screenFullImage);

                        JSONObject payload = new JSONObject();
                        payload.put("image", image);
                        payload.put("text", outText);

                        apiResponse.setSuccess(true);
                        apiResponse.setPayload(payload);
                    } catch (TesseractException e) {
                        e.printStackTrace();
                    }

                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
                return apiResponse;
            }

        };
    }
}
