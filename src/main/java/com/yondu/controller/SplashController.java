package com.yondu.controller;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.AppConfigConstants;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;

/** Splash Stage/Screen Controller mapped to splash.xml
 *
 *  @author m1d0rf33d
 */
public class SplashController implements Initializable{
/*
    @FXML
    public  ProgressBar myProgressBar;*/
    @FXML
    public Label progressStatus;
    @FXML
    public ImageView rushLogoImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        MyService myService = new MyService();
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {

                Stage stage = new Stage();
                stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
                stage.setMaximized(true);
                stage.show();
                App.appContextHolder.setHomeStage(stage);
                ((Stage) rushLogoImage.getScene().getWindow()).close();
            }
        });
        //rushLogoImage.progressProperty().bind(myService.progressProperty());
        progressStatus.textProperty().bind(myService.messageProperty());
        myService.start();
    }

    private class MyService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    //updateMessage("Checking system configuration..  ..");
                    //create ocr folder
                    File dir = new File(System.getProperty("user.home") + "\\Rush-POS-Sync");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    File file = new File(System.getProperty("user.home") + AppConfigConstants.OCR_CONFIG_LOCATION);
                    if (!file.exists()) {
                        file.createNewFile();
                        PrintWriter fstream = new PrintWriter(new FileWriter(file));
                        fstream.println("sales_pos_x=");
                        fstream.println("sales_pos_y=");
                        fstream.println("sales_width=");
                        fstream.println("sales_height=");
                        fstream.println("or_pos_x=");
                        fstream.println("or_pos_y=");
                        fstream.println("or_width=");
                        fstream.println("or_height=");
                        fstream.flush();
                        fstream.close();
                    }
                    Thread.sleep(1000);
                    /* updateProgress(20, 100);
                    updateMessage("Checking connectivity");
                    Thread.sleep(2000);

                    updateProgress(60, 100);
                    updateMessage("Checking accounts..");
                    Thread.sleep(2000);
                    updateProgress(2000, 100);
                    updateMessage("Coompleted");*/
                    return null;
                }
            };
        }
    }
}
