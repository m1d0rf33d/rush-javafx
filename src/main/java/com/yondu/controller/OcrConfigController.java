package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by aomine on 9/29/16.
 */
public class OcrConfigController implements Initializable{

    private static final String SALES_CAPTURE_FXML = "/app/fxml/sales-capture.fxml";
    private static final String PREVIEW_SALES_FXML = "/app/fxml/sales-preview.fxml";
    private static final String OR_CAPTURE_FXML = "/app/fxml/ornumber-capture.fxml";
    private Stage salesCaptureStage;
    private Stage previewSalesStage;
    private Stage orCaptureStage;
    @FXML
    public javafx.scene.control.Label salesDimensionLabel;
    @FXML
    public javafx.scene.control.Label ornumberDimensionLabel;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Load ocr-properties saved config
        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File("/home/aomine/Desktop/ocr-config.properties"));
            prop.load(inputStream);
            StringBuilder sb = new StringBuilder();
            sb.append(prop.getProperty("sales_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_width"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_height"));
            this.salesDimensionLabel.setText(sb.toString());

            sb = new StringBuilder();
            sb.append(prop.getProperty("or_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("or_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("or_width"));
            sb.append(", ");
            sb.append(prop.getProperty("or_height"));
            this.ornumberDimensionLabel.setText(sb.toString());
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
            this.salesDimensionLabel.setText(sb.toString());
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
            this.ornumberDimensionLabel.setText(sb.toString());
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
            InputStream inputStream = new FileInputStream(new File("/home/aomine/Desktop/ocr-config.properties"));
            prop.load(inputStream);
            StringBuilder sb = new StringBuilder();
            sb.append(prop.getProperty("sales_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_width"));
            sb.append(", ");
            sb.append(prop.getProperty("sales_height"));
            this.salesDimensionLabel.setText(sb.toString());

            sb = new StringBuilder();
            sb.append(prop.getProperty("or_pos_x"));
            sb.append(", ");
            sb.append(prop.getProperty("or_pos_y"));
            sb.append(", ");
            sb.append(prop.getProperty("or_width"));
            sb.append(", ");
            sb.append(prop.getProperty("or_height"));
            this.ornumberDimensionLabel.setText(sb.toString());
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

            File file = new File("/home/aomine/Desktop/ocr-config.properties");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
