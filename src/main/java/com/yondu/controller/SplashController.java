package com.yondu.controller;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.ApiService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_FXML;

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

    private ApiService apiService;
    private String baseUrl;
    private String getBranchesEndpoint;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService();
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        MyService myService = new MyService();
        myService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                if (App.appContextHolder.isOnlineMode()) {
                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
                    stage.setMaximized(true);
                    stage.show();
                    App.appContextHolder.setHomeStage(stage);
                    ((Stage) rushLogoImage.getScene().getWindow()).close();
                } else {

                    try {
                        Stage givePointsStage = new Stage();
                        Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
                        givePointsStage.setScene(new Scene(root, 400,220));
                        givePointsStage.setTitle("Give Points");
                        givePointsStage.resizableProperty().setValue(Boolean.FALSE);
                        givePointsStage.show();
                        ((Stage) rushLogoImage.getScene().getWindow()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
                protected Void call() {
                    //Prepare configuration files
                    try {
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
                        //Check connection

                        Properties prop = new Properties();
                        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
                        if (inputStream != null) {
                            prop.load(inputStream);
                        } else {
                            throw new FileNotFoundException("property file api.properties not found in the classpath");
                        }
                        baseUrl = prop.getProperty("base_url");
                        getBranchesEndpoint = prop.getProperty("get_branches_endpoint");
                        String url = baseUrl + getBranchesEndpoint;
                        List<NameValuePair> params = new ArrayList<>();
                        String result = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                        App.appContextHolder.setOnlineMode(true);
                        Thread.sleep(1000);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        App.appContextHolder.setOnlineMode(false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };
        }
    }
}
