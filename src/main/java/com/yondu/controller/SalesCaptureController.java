package com.yondu.controller;

import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by aomine on 9/29/16.
 */
public class SalesCaptureController {

    public Button previewButton;
    private Double x;
    private Double y;
    private Double width;
    private Double height;

    public void captureSalesArea() {
        Stage stage = (Stage) this.previewButton.getScene().getWindow();
        this.x = stage.getX();
        this.y = stage.getY();
        this.width = stage.getWidth();
        this.height = stage.getHeight();

        try {

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
