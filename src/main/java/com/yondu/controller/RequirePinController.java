package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.service.ApiService;
import com.yondu.service.RouteService;
import com.yondu.utils.ButtonEventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by erwin on 10/27/2016.
 */
public class RequirePinController  implements Initializable {


    @FXML
    public Pane numbersPane;
    @FXML
    public TextField pinTextField;
    @FXML
    public Button cancelBtn;
    @FXML
    public ImageView rushLogo;
    @FXML
    public ImageView removeImage;
    @FXML
    public Button proceedBtn;
    @FXML
    public Pane overlayPane2;

    private Pane overlayPane;
    private String login;
    private String branchId;

    private RouteService routeService = new RouteService();
    private ApiService apiService = new ApiService();
    private Stage currentStage;

    public RequirePinController(Pane overlayPane, String login, String branchId) {
        this.overlayPane = overlayPane;
        this.login = login;
        this.branchId = branchId;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentStage = (Stage) cancelBtn.getScene().getWindow();
        pinTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }
        });

        pinTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    pinTextField.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (pinTextField.getText().length() > 4) {
                    String s = pinTextField.getText().substring(0, 4);
                    pinTextField.setText(s);
                }
            }
        });
        overlayPane2.setVisible(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        overlayPane2.setPrefHeight(height);
        overlayPane2.setPrefWidth(width);

        removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.pinTextField.setText("");
        });


        rushLogo.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));
        removeImage.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/remove.png").toExternalForm()));

        cancelBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            overlayPane.setVisible(false);
            ((Stage) cancelBtn.getScene().getWindow()).close();
                });

        proceedBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            //Build request body
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, this.login));
            params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, branchId));
            params.add(new BasicNameValuePair(ApiFieldContants.PIN, this.pinTextField.getText()));
            String url = BASE_URL + LOGIN_ENDPOINT;
            JSONObject jsonObject = apiService.call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

            if (jsonObject != null) {
                if (jsonObject.get("error_code").equals("0x0")) {
                    JSONObject data = (JSONObject) jsonObject.get("data");
                    App.appContextHolder.setEmployeeName(((String) data.get("name")));
                    App.appContextHolder.setEmployeeId((String) data.get("id"));
                    App.appContextHolder.setBranchId(branchId);
                    //Redirect to home page
                    routeService.goToHomeScreen(currentStage);
                } else {
                    overlayPane2.setVisible(true);
                    prompt((String) jsonObject.get("message"), event);
                }
            } else {
                //TODO: offline
            }
        });

        List<Node> buttons = numbersPane.getChildren();
        for (Node button : buttons) {
            try {
                Button b = (Button) button;
                b.setOnAction(new ButtonEventHandler(pinTextField));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private void prompt(String message, MouseEvent event) {

        try {
            Stage stage = new Stage();
            FXMLLoader loader  = new FXMLLoader(App.class.getResource("/app/fxml/custom-dialog.fxml"));
            CustomDialogController controller = new CustomDialogController(message, overlayPane2);
            loader.setController(controller);
            stage.setScene(new Scene(loader.load(), 500,350));
            stage.setTitle("Rush POS Sync");
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