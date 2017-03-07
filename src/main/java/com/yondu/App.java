package com.yondu;

import com.yondu.service.RouteService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

import static com.yondu.model.constants.AppConfigConstants.*;

/** This is where the fun begins..
 *
 *  @author m1d0rf33d
 */
public class App extends Application{

    public static final AppContextHolder appContextHolder = new AppContextHolder();

    private RouteService routeService = new RouteService();

    public static void main(String[] args) {

        if (System.getProperty("os.name").contains("Windows")) {
            if (System.getenv("ProgramFiles(x86)") != null) {
                TESSERACT_HOME = "c:\\Program Files (x86)\\" + RUSH_FOLDER + "\\" + TESSERACT_FOLDER;
            } else {
                TESSERACT_HOME = "c:\\Program Files\\" + RUSH_FOLDER + "\\" + TESSERACT_FOLDER;
            }
        }
        RUSH_HOME = System.getenv("RUSH_HOME").replace(";", "");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        File file = new File(RUSH_HOME + DIVIDER + LOCK_FILE);
        if (!file.exists()) {
            file.createNewFile();
        }
         routeService.goToLoginScreen(null);
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


