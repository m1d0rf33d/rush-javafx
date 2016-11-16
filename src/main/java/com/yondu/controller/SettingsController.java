package com.yondu.controller;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.utils.ResizeHelper;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

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
    @FXML
    public javafx.scene.control.Button setOrButton;
    @FXML
    public Button OrCaptureButton;

    @FXML
    public javafx.scene.control.Button setSalesButton;
    @FXML
    public Button SalesCaptureButton;

    public void loadSalesCaptureArea() {
        try {
            if (salesCaptureStage != null) {
                salesCaptureStage.close();
            }
            salesCaptureStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(SALES_CAPTURE_FXML));
            salesCaptureStage.setScene(new Scene(root, 300,100));
            salesCaptureStage.initStyle(StageStyle.UNDECORATED);
            salesCaptureStage.setMaxHeight(100);
            salesCaptureStage.setMaxWidth(300);
            salesCaptureStage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            salesCaptureStage.show();
            App.appContextHolder.setSalesCaptureStage(salesCaptureStage);
            ResizeHelper.addResizeListener(salesCaptureStage);
            setSalesButton.setVisible(false);
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
            orCaptureStage.initStyle(StageStyle.UNDECORATED);
            orCaptureStage.setScene(new Scene(root, 300,100));
            orCaptureStage.setMaxHeight(100);
            orCaptureStage.setMaxWidth(300);
            App.appContextHolder.setOrCaptureStage(orCaptureStage);
            ResizeHelper.addResizeListener(orCaptureStage);
            orCaptureStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            orCaptureStage.show();
            setOrButton.setVisible(false);
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
                InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);
            this.previewImage.setImage(image);
            String basePath = "";
            if (App.appContextHolder.getIs64Bit()) {
                basePath = "C:\\Program Files (x86)";
            } else {
                basePath = "C:\\Program Files";
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(basePath + TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            this.previewText.setText(outText);

        } catch (AWTException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.SalesCaptureButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Double posX = salesCaptureStage.getX(),
                        posY = salesCaptureStage.getY(),
                        width = salesCaptureStage.getWidth(),
                        height = salesCaptureStage.getHeight();

                App.appContextHolder.setSalesPosX(posX.intValue());
                App.appContextHolder.setSalesPosY(posY.intValue());
                App.appContextHolder.setSalesWidth(width.intValue());
                App.appContextHolder.setSalesHeight(height.intValue());

                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Target screen area captured.", ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {
                    alert.close();
                }
                salesCaptureStage.close();
                setSalesButton.setVisible(true);
                salesAreaLbl.setText(posX.intValue() + ", " + posY.intValue() + ", " + width.intValue() + ", " + height.intValue());
            }
        });

        this.OrCaptureButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Double posX = orCaptureStage.getX(),
                        posY = orCaptureStage.getY(),
                        width = orCaptureStage.getWidth(),
                        height = orCaptureStage.getHeight();

                App.appContextHolder.setOrNumberPosX(posX.intValue());
                App.appContextHolder.setOrNumberPosY(posY.intValue());
                App.appContextHolder.setOrNumberWidth(width.intValue());
                App.appContextHolder.setOrNumberHeight(height.intValue());

                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Target screen area captured.", ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {
                    alert.close();
                }
                orCaptureStage.close();
                setOrButton.setVisible(true);
                orAreaLbl.setText(posX.intValue() + ", " + posY.intValue() + ", " + width.intValue() + ", " + height.intValue());
            }
        });


        //Load ocr-properties saved config
        try {
            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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
            InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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

            File file = new File(App.appContextHolder.getOcrFullPath());
            if (file.exists()) {
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(file);
                prop.load(inputStream);
                salesPosX = prop.getProperty("sales_pos_x");
                salesPosY = prop.getProperty("sales_pos_y");
                salesWidth = prop.getProperty("sales_width");
                salesHeight = prop.getProperty("sales_height");

                orPosX = prop.getProperty("or_pos_x");
                orPosY = prop.getProperty("or_pos_y");
                orWidth = prop.getProperty("or_width");
                orHeight = prop.getProperty("or_height");
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

            if (orPosX ==null  || salesPosX == null || orPosX.equals("") || salesPosX.equals("")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"You have not completed assigning target screen area.", ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {
                    alert.close();
                }
            } else {
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

                Alert alert = new Alert(Alert.AlertType.INFORMATION,"OCR settings updated.", ButtonType.OK);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.OK) {

                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Browser(),750,500, javafx.scene.paint.Color.web("#666970")));
                    stage.setMaximized(true);
                    stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                    stage.show();
                    App.appContextHolder.setHomeStage(stage);
                    if (salesCaptureStage != null) {
                        ((Stage)salesCaptureStage.getScene().getWindow()).close();
                    }
                    if (orCaptureStage != null) {
                        ((Stage)orCaptureStage.getScene().getWindow()).close();
                    }
                    ((Stage)this.previewText.getScene().getWindow()).close();
                    alert.close();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void previewOrArea() {

        Integer salesX = null, salesY = null, salesWidth = null, salesHeight = null;

        //Check if user captured temporary values if not get configuration from config file
        if (App.appContextHolder.getOrNumberPosX() != null) {
            //Create image based from temporary ocr config
            salesX      = App.appContextHolder.getOrNumberPosX();
            salesY      = App.appContextHolder.getOrNumberPosY();
            salesWidth  = App.appContextHolder.getOrNumberWidth();
            salesHeight = App.appContextHolder.getOrNumberHeight();
        } else {
            try {
                Properties prop = new Properties();
                InputStream inputStream = new FileInputStream(App.appContextHolder.getOcrFullPath());
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
            Rectangle screen = new Rectangle(salesX, salesY, salesWidth, salesHeight);

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(screenFullImage, null);
            this.previewImage.setImage(image);

            String basePath = "";
            if (App.appContextHolder.getIs64Bit()) {
                basePath = "C:\\Program Files (x86)";
            } else {
                basePath = "C:\\Program Files";
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(basePath + TESSERACT_LOCATION);
            tesseract.setLanguage("eng");
            // Get OCR result
            String outText = null;
            try {
                outText = tesseract.doOCR(screenFullImage);
            } catch (TesseractException e) {
                e.printStackTrace();
            }
            this.previewText.setText(outText);

        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    public void exit() {

        Stage stage = new Stage();
        stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
        stage.setMaximized(true);
        stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
        stage.show();
        App.appContextHolder.setHomeStage(stage);

        if (salesCaptureStage != null) {
            ((Stage)salesCaptureStage.getScene().getWindow()).close();
        }
        if (orCaptureStage != null) {
            ((Stage)orCaptureStage.getScene().getWindow()).close();
        }
        ((Stage)this.previewText.getScene().getWindow()).close();
    }


}
