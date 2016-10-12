package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppConfigConstants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by erwin on 10/11/2016.
 */
public class ActivationController implements Initializable{

    @FXML
    public Button activateBtn;

    @FXML
    public TextField merchantKey;
    @FXML
    public ImageView rushLogo;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rushLogo.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        activateBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            activate();
        });
    }
    private void activate() {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream("api.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            String inputKey = merchantKey.getText();
            if (inputKey.equals(props.get("planet_sports_key"))) {
                File file = new File(System.getProperty("user.home") + AppConfigConstants.ACTIVATION_LOCATION);
                PrintWriter writer = new PrintWriter(file);
                writer.write("merchant=planet_sports");
                writer.flush();
                writer.close();
                goToLoginPage();
            }else if (inputKey.equals(props.get("pro_gross_key"))) {
                File file = new File(System.getProperty("user.home") + AppConfigConstants.ACTIVATION_LOCATION);
                PrintWriter writer = new PrintWriter(file);
                writer.write("merchant=pro_gross");
                writer.flush();
                writer.close();
                goToLoginPage();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid merchant key.");
                alert.showAndWait();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void goToLoginPage() {
        ((Stage) activateBtn.getScene().getWindow()).close();
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.SPLASH_FXML));
            stage.setScene(new Scene(root, 600,400));
            stage.resizableProperty().setValue(false);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
