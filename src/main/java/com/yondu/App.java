package com.yondu;

import com.yondu.model.constants.AppConfigConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** This is where the fun begins..
 *
 *  @author m1d0rf33d
 */
public class App extends Application{

    public static final AppContextHolder appContextHolder = new AppContextHolder();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Launch Splash stage
        Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
        primaryStage.setScene(new Scene(root, 600,400));
        primaryStage.resizableProperty().setValue(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

}


