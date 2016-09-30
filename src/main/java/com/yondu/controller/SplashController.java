package com.yondu.controller;

import com.yondu.Browser;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by aomine on 9/30/16.
 */
public class SplashController implements Initializable{

    @FXML
    public  ProgressBar myProgressBar;
    @FXML
    public Label progressStatus;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        MyService myService = new MyService();
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                Stage stage = new Stage();
                stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
                stage.show();

                ((Stage) myProgressBar.getScene().getWindow()).close();
            }
        });
        myProgressBar.progressProperty().bind(myService.progressProperty());
        progressStatus.textProperty().bind(myService.messageProperty());
        myService.start();
    }

    private class MyService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateProgress(20, 100);
                    updateMessage("Checking connectivity");
                    Thread.sleep(2000);

                    updateProgress(60, 100);
                    updateMessage("Checking accounts..");
                    Thread.sleep(2000);
                    updateProgress(2000, 100);
                    updateMessage("Coompleted");
                    return null;
                }
            };
        }
    }
}