package com.yondu;

import com.yondu.model.ApiResponse;
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
            if (System.getenv("ProgramFiles(x86)") != null) {
                appContextHolder.TESSERACT_HOME = "c:\\Program Files (x86)\\" + RUSH_FOLDER + "\\" + TESSERACT_FOLDER;
            } else {
                appContextHolder.TESSERACT_HOME = "c:\\Program Files\\" + RUSH_FOLDER + "\\" + TESSERACT_FOLDER;
            }
        }
        RUSH_HOME = System.getenv("RUSH_HOME").replace(";", "");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        File file = new File(RUSH_HOME + DIVIDER + LOCK_FILE);
        if (file.exists()) {
            file.createNewFile();
        }
         appContextHolder.getRouteService().goToLoginScreen(null);
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        File file = new File(RUSH_HOME + DIVIDER + LOCK_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

}


