package com.yondu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;


public class App extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
       //FXML approach
        /* Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("RUSH POS");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();*/

        //Web View approach
        primaryStage.setTitle("Web View");
        primaryStage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
        primaryStage.show();
    }

}


