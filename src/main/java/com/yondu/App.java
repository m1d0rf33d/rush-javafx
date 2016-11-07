package com.yondu;

import com.yondu.model.constants.AppConfigConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/** This is where the fun begins..
 *
 *  @author m1d0rf33d
 */
public class App extends Application{

    public static final AppContextHolder appContextHolder = new AppContextHolder();

    public static void main(String[] args) {


        //Check lock file
        File file = new File(System.getProperty("user.home") + "\\Rush-POS-Sync\\lock.txt");
        if (file.exists()) {
            try {
                Runtime.getRuntime().exec("cmd /c start C:\\\"Program Files (x86)\"\\Rush-POS-Sync\\max.vbs");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Let's get the party started
        Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
        primaryStage.setScene(new Scene(root, 600,400));
        primaryStage.resizableProperty().setValue(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        File file = new File(System.getProperty("user.home") + "\\Rush-POS-Sync\\lock.txt");
        if (file.exists()) {
            file.delete();
        }
    }
}


