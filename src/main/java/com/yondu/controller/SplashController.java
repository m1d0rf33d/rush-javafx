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

import java.awt.*;
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
    @FXML
    public Label progressStatus;
    @FXML
    public ImageView rushLogoImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rushLogoImage.setImage(new Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        MyService myService = new MyService();
        myService.setOnSucceeded((WorkerStateEvent t) -> {
            if (App.appContextHolder.getIsActivated().equals("true")) {
                //Check ONLINE of OFFLINE mode
                if (App.appContextHolder.isOnlineMode()) {
                /*Stage stage = new Stage();
                stage.setScene(new Scene(new Browser(),750,500, Color.web("#666970")));
                stage.setMaximized(true);
                stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                stage.show();
                App.appContextHolder.setHomeStage(stage);*/
                    try {
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        double width = screenSize.getWidth();
                        double height = screenSize.getHeight();
                        Stage stage = new Stage();
                        Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/login.fxml"));
                        stage.setScene(new Scene(root, width,height - 70));
                        stage.setTitle("Rush");
                        stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                        stage.show();
                        ((Stage) rushLogoImage.getScene().getWindow()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                    try {
                        Stage givePointsStage = new Stage();
                        Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
                        givePointsStage.setScene(new Scene(root, 400,220));
                        givePointsStage.setTitle("Give Points");
                        givePointsStage.resizableProperty().setValue(Boolean.FALSE);
                        givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                        givePointsStage.show();
                        ((Stage) rushLogoImage.getScene().getWindow()).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
               try {
                   Stage stage = new Stage();
                   Parent root = FXMLLoader.load(App.class.getResource("/app/fxml/activation.fxml"));
                   stage.setScene(new Scene(root, 400,200));
                   stage.setTitle("Activate merchant");
                   stage.resizableProperty().setValue(Boolean.FALSE);
                   stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                   stage.show();
                   ((Stage) rushLogoImage.getScene().getWindow()).close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
            }
        });
        progressStatus.textProperty().bind(myService.messageProperty());
        myService.start();
    }

    private class MyService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() {

                    try {
                        //Prepare configuration files
                       File dir = new File(System.getProperty("user.home") + "\\Rush-POS-Sync");
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File offlineFile = new File(System.getProperty("user.home") + AppConfigConstants.OFFLINE_LOCATION);
                        if (!offlineFile.exists()) {
                            offlineFile.createNewFile();
                        }
                        //File file = new File("/home/aomine/Desktop/ocr.properties");
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
                        App.appContextHolder.setOcrFullPath(file.getAbsolutePath());
                        App.appContextHolder.setOfflinePath(System.getProperty("user.home") + AppConfigConstants.OFFLINE_LOCATION);


                        loadEndpointsFromConfig();

                        //Check internet connection
                        ApiService apiService = new ApiService();
                        App.appContextHolder.setApiService(apiService);
                        String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetBranchesEndpoint();
                        List<NameValuePair> params = new ArrayList<>();
                        apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                        App.appContextHolder.setOnlineMode(true);
                        /*App.appContextHolder.setOfflinePath("/home/aomine/Desktop/offline.txt");
                        App.appContextHolder.setOcrFullPath("/home/aomine/Desktop/ocr.properties");*/


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        App.appContextHolder.setOnlineMode(false);
                    }
                    return null;
                }
            };
        }
    }

    private void loadEndpointsFromConfig() {
       try {
           Properties prop = new Properties();
           InputStream inputStream = getClass().getClassLoader().getResourceAsStream("api.properties");
           if (inputStream != null) {
               prop.load(inputStream);
               inputStream.close();
           } else {
               throw new FileNotFoundException("property file api.properties not found in the classpath");
           }
           App.appContextHolder.setBaseUrl(prop.getProperty("base_url"));
           App.appContextHolder.setRegisterEndpoint(prop.getProperty("register_endpoint"));
           App.appContextHolder.setMemberLoginEndpoint(prop.getProperty("member_login_endpoint"));
           App.appContextHolder.setPointsConversionEndpoint(prop.getProperty("points_conversion_endpoint"));
           App.appContextHolder.setGivePointsEndpoint(prop.getProperty("give_points_endpoint"));
           App.appContextHolder.setGetPointsEndpoint(prop.getProperty("get_points_endpoint"));
           App.appContextHolder.setPayWithPointsEndpoint(prop.getProperty("pay_points_endpoint"));
           App.appContextHolder.setGetRewardsEndpoint(prop.getProperty("get_rewards_endpoint"));
           App.appContextHolder.setRedeemRewardsEndpoint(prop.getProperty("redeem_rewards_endpoint"));
           App.appContextHolder.setUnclaimedRewardsEndpoint(prop.getProperty("unclaimed_rewards_endpoint"));
           App.appContextHolder.setClaimRewardsEndpoint(prop.getProperty("claim_rewards_endpoint"));
           App.appContextHolder.setGetRewardsMerchantEndpoint(prop.getProperty("get_rewards_merchant_endpoint"));
           App.appContextHolder.setCustomerRewardsEndpoint(prop.getProperty("customer_rewards_endpoint"));
           App.appContextHolder.setCustomerTransactionsEndpoint(prop.getProperty("customer_transactions_endpoint"));
           App.appContextHolder.setGetBranchesEndpoint(prop.getProperty("get_branches_endpoint"));
           App.appContextHolder.setLoginEndpoint(prop.getProperty("login_endpoint"));
           App.appContextHolder.setAuthorizationEndpoint(prop.getProperty("authorization_endpoint"));
           App.appContextHolder.setAppKey(prop.getProperty("app_key"));
           App.appContextHolder.setAppSecret(prop.getProperty("app_secret"));
           App.appContextHolder.setCustomerAppKey(prop.getProperty("customer_app_key"));
           App.appContextHolder.setCustomerAppSecret(prop.getProperty("customer_app_secret"));
           App.appContextHolder.setIsActivated(prop.getProperty("isActivated"));
       } catch(IOException e) {
           e.printStackTrace();
       }
    }
}
