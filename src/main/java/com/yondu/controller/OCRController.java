package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.RouteService;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/15/17.
 */
public class OCRController implements Initializable {

    @FXML
    public ImageView orPosImageView;
    @FXML
    public ImageView amountPosImageView;
    @FXML
    public Button saveButton;
    @FXML
    public TextField orPosTextField;
    @FXML
    public TextField amountPosTextField;
    @FXML
    public ImageView orPreviewImageView;
    @FXML
    public ImageView amountPreviewImageView;
    @FXML
    public ImageView previewImageView;
    @FXML
    public Label previewLabel;

    private Double orPosX;
    private Double orPosY;
    private Double orWidth;
    private Double orHeight;
    private Double amountPosX;
    private Double amountPosY;
    private Double amountWidth;
    private Double amountHeight;

    private RouteService routeService = new RouteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/edit.png").toExternalForm()));
        amountPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/edit.png").toExternalForm()));
        orPreviewImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/preview.png").toExternalForm()));
        amountPreviewImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/preview.png").toExternalForm()));

        orPosImageView.setOnMouseClicked((MouseEvent e) -> {

            if (App.appContextHolder.getOrCaptureStage() != null) {
                orPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/edit.png").toExternalForm()));
                Stage stage = App.appContextHolder.getOrCaptureStage();
                orPosX = stage.getX();
                orPosY = stage.getY();
                orWidth = stage.getWidth();
                orHeight = stage.getHeight();
                String pos = orPosX + " " + orPosY + " "  + orWidth + " " + orHeight;
                orPosTextField.setText(pos);
                App.appContextHolder.getOrCaptureStage().close();
                App.appContextHolder.setOrCaptureStage(null);

            } else {
                orPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/save.png").toExternalForm()));
                routeService.loadOrCaptureScreen();
            }
        });

        amountPosImageView.setOnMouseClicked((MouseEvent e) -> {

            if (App.appContextHolder.getSalesCaptureStage() != null) {
                amountPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/edit.png").toExternalForm()));
                Stage stage = App.appContextHolder.getSalesCaptureStage();
                amountPosX = stage.getX();
                amountPosY = stage.getY();
                amountWidth = stage.getWidth();
                amountHeight = stage.getHeight();
                String pos = amountPosX + " " + amountPosY + " "  + amountWidth + " " + amountHeight;
                amountPosTextField.setText(pos);
                App.appContextHolder.getSalesCaptureStage().close();
                App.appContextHolder.setSalesCaptureStage(null);

            } else {
                amountPosImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/save.png").toExternalForm()));
                routeService.loadSalesCaptureScreen();
            }
        });

        saveButton.setOnMouseClicked((MouseEvent e) -> {
            saveDimensions();
        });

        orPreviewImageView.setOnMouseClicked((MouseEvent e) -> {
            preview("or");
        });

        amountPreviewImageView.setOnMouseClicked((MouseEvent e) -> {
            preview("amount");
        });
    }

    public void saveDimensions() {
        try {

            File file = new File(System.getenv("RUSH_HOME") + DIVIDER + OCR_PROPERTIES);

            if (!file.exists()) {
                file.createNewFile();
            }

            if (orPosX != null && amountPosX != null) {
                PrintWriter fstream = new PrintWriter(new FileWriter(file));
                fstream.println("sales_pos_x=" + amountPosX);
                fstream.println("sales_pos_y=" + amountPosY);
                fstream.println("sales_width=" + amountWidth);
                fstream.println("sales_height=" + amountHeight);
                fstream.println("or_pos_x=" + orPosX);
                fstream.println("or_pos_y=" + orPosY);
                fstream.println("or_width=" + orWidth);
                fstream.println("or_height=" + orHeight);
                fstream.flush();
                fstream.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION,"OCR settings updated.", ButtonType.OK);
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.showAndWait();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void preview(String field) {

        int x, y , w, h;
        if (field.equals("or")) {
            x = orPosX.intValue();
            y = orPosY.intValue();
            h = orHeight.intValue();
            w = orWidth.intValue();
        } else {
            x = amountPosX.intValue();
            y = amountPosY.intValue();
            h = amountHeight.intValue();
            w = amountWidth.intValue();
        }

        try {
            Robot robot = new Robot();
            Rectangle screen = new Rectangle(x, y, w, h);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);
            this.previewImageView.setImage(image);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(System.getenv("RUSH_HOME") + DIVIDER + TESSERACT_FOLDER);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            this.previewLabel.setText(outText);

        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
}
