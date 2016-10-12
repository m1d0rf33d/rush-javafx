package com.yondu.controller;

import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.utils.ButtonEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_DETAILS_FXML;

/**
 * Created by erwin on 10/10/2016.
 */
public class LoginController implements Initializable {

    @FXML
    public ImageView rushLogo;
    @FXML
    public ComboBox branchBox;
    @FXML
    public Pane mainPane;
    @FXML
    public Pane rightPane;
    @FXML
    public javafx.scene.control.Button loginBtn;
    @FXML
    public TextField loginTextField;
    @FXML
    public Pane overlayPane;
    @FXML
    public ImageView removeImage;

    private List<JSONObject> branches;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        List<Node> buttons = rightPane.getChildren();
        for (Node button : buttons) {
            try {
                Button b = (Button) button;
                b.setOnAction(new ButtonEventHandler(loginTextField));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        overlayPane.setVisible(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        mainPane.setPrefWidth(width);
        mainPane.setPrefHeight(height);
        rightPane.setPrefHeight(height);
        rightPane.setPrefWidth(width / 2);
        rightPane.setLayoutX(width / 2);
        overlayPane.setPrefHeight(height);
        overlayPane.setPrefWidth(width);

        rushLogo.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        removeImage.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/remove.png").toExternalForm()));
        try {
            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetBranchesEndpoint();
            java.util.List<NameValuePair> params = new ArrayList<>();
            String jsonResponse = App.appContextHolder.getApiService().call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            List<JSONObject> data = (ArrayList) jsonObj.get("data");
            branches = data;
            for (JSONObject branch : data) {
                branchBox.getItems().add(branch.get("name"));
            }
            branchBox.getSelectionModel().selectFirst();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            overlayPane.setVisible(true);
            if (branches != null && branches.size() > 0) {
                String branchId = "";
                for (JSONObject branch : branches) {
                    if (branchBox.getSelectionModel().getSelectedItem().equals(branch.get("name"))) {
                        branchId = (String) branch.get("id");
                    }
                }

                String jsonResponse = null;
                try {
                    //Build request body
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, loginTextField.getText()));
                    params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, branchId));
                    String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getLoginEndpoint();
                    jsonResponse = App.appContextHolder.getApiService().call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
                    if (jsonObject.get("error_code").equals("0x0")) {
                        JSONObject data = (JSONObject) jsonObject.get("data");
                        App.appContextHolder.setEmployeeName(((String) data.get("name")));
                        App.appContextHolder.setEmployeeId((String) data.get("id"));

                        //Redirect to home page
                        Stage stage = new Stage();
                        stage.setScene(new Scene(new Browser(),750,500, javafx.scene.paint.Color.web("#666970")));
                        stage.setMaximized(true);
                        stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                        stage.show();
                        App.appContextHolder.setHomeStage(stage);
                        ((Stage) branchBox.getScene().getWindow()).close();
                    } else {
                       prompt((String) jsonObject.get("message"), event);
                    }

                } catch (IOException e) {
                    //LOG here
                    jsonResponse = null;
                    App.appContextHolder.setOnlineMode(false);
                    prompt("Unable to connect to Rush Server due to network connection problem. Please check your internet connection and try again.", event);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } else {
                prompt("Unable to connect to Rush Server due to network connection problem. Please check your internet connection and try again.", event);
            }
        });
    }

    private void prompt(String message, MouseEvent event) {

        try {
            Stage stage = new Stage();
            FXMLLoader  loader  = new FXMLLoader(App.class.getResource("/app/fxml/custom-dialog.fxml"));
            CustomDialogController controller = new CustomDialogController(message, overlayPane);
            loader.setController(controller);
            stage.setScene(new Scene(loader.load(), 500,350));
            stage.setTitle("Rush");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(
                    ((Node)event.getSource()).getScene().getWindow() );
            stage.resizableProperty().setValue(Boolean.FALSE);
            stage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
