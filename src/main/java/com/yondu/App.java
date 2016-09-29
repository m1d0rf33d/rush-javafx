package com.yondu;

import com.yondu.model.Account;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;

import java.io.*;


public class App extends Application{

    public static final AppContextHolder appContextHolder = new AppContextHolder();

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            File file = new File("C:\\Users\\erwin\\Desktop\\rushlogs.txt");
            try {
                file.createNewFile();
                PrintWriter out = new PrintWriter(new FileWriter(file, true));
                out.write(e.getMessage());
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("RUSH POS - SYNC");
        primaryStage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
        primaryStage.show();
    }

}


