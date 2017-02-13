package com.yondu;

import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.RouteService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import static com.yondu.model.constants.AppConfigConstants.*;

/** This is where the fun begins..
 *
 *  @author m1d0rf33d
 */
public class App extends Application{

    public static final AppContextHolder appContextHolder = new AppContextHolder();

    public static void main(String[] args) {

        if (System.getProperty("os.name").contains("Windows")) {
            DIVIDER = "\\";

            File file = new File(System.getenv("RUSH_HOME") + DIVIDER + LOCK_FILE);
            if (file.exists()) {
                try {
                    Runtime.getRuntime().exec("cmd /c start  " + System.getenv("RUSH_HOME") + DIVIDER + VBS_FILE);
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
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        File file = new File(System.getenv("RUSH_HOME") + DIVIDER + LOCK_FILE);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                });
                launch(args);
            }
        } else {
            // Either MAC or Linux operating system. MAC will crash this is only for linux
            AppConfigConstants.DIVIDER = "//";
            launch(args);
        }

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
        File file = new File(System.getenv("RUSH_HOME") + DIVIDER + LOCK_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}


