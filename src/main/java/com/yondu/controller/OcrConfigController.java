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
    private static final String PREVIEW_SALES_FXML = "/app/fxml/sales-preview.fxml";
    private Stage salesCaptureStage;
    private Stage previewSalesStage;

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

    public void previewSalesCaptureArea() {

        try {
            if (previewSalesStage != null) {
                previewSalesStage.close();
            }
            previewSalesStage = new Stage();
            Parent root = null;
            root = FXMLLoader.load(App.class.getResource(PREVIEW_SALES_FXML));
            previewSalesStage.setScene(new Scene(root, 600,400));
            previewSalesStage.resizableProperty().setValue(false);
            previewSalesStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
