package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by aomine on 9/29/16.
 */
public class SalesCaptureController {

    private static final String CAPTURE_RESULT_FXML = "/app/fxml/capture-result.fxml";

    @FXML
    public Button previewButton;

    private Stage resultStage;

    public void captureSalesArea() {
        Stage stage = (Stage) this.previewButton.getScene().getWindow();
        Double salesPosX = stage.getX(),
                salesPosY = stage.getY(),
                salesWidth = stage.getWidth(),
                salesHeight = stage.getHeight();

        App.appContextHolder.setSalesPosX(salesPosX.intValue());
        App.appContextHolder.setSalesPosY(salesPosY.intValue());
        App.appContextHolder.setSalesWidth(salesWidth.intValue());
        App.appContextHolder.setSalesHeight(salesHeight.intValue());

        /*try {

            File file = new File("/home/aomine/Desktop/ocr-config.properties");
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter fstream = new PrintWriter(new FileWriter(file));
            fstream.println("sales_pos_x=" + String.valueOf(stage.getX()));
            fstream.println("sales_pos_y=" + String.valueOf(stage.getY()));
            fstream.println("sales_width=" + String.valueOf(stage.getWidth()));
            fstream.println("sales_height=" + String.valueOf(stage.getHeight()));
            fstream.flush();
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        Stage resultStage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(App.class.getResource(CAPTURE_RESULT_FXML));
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultStage.setScene(new Scene(root, 400,150));
        resultStage.setX(400);
        resultStage.setY(400);
        resultStage.resizableProperty().setValue(false);
        resultStage.show();

        ((Stage) this.previewButton.getScene().getWindow()).close();
    }

    /*public void handleSalesView() {
        try {
            Robot robot = new Robot();

            Toolkit myToolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = myToolkit.getScreenSize();

            Rectangle screen = new Rectangle(x.intValue(), y.intValue(), width.intValue(),height.intValue());

            BufferedImage screenFullImage = robot.createScreenCapture(screen);
            ImageIO.write(screenFullImage, "jpg", new File("/home/aomine/Desktop/ss.jpg"));

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }
    }*/
}
