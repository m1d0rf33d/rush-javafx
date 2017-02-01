package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.Browser;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.ButtonEventHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML
    public Label offlineLbl;
    @FXML
    public Button givePointsBtn;
    @FXML
    public Button reconnectBtn;
    @FXML
    public Label employeeLbl;
    @FXML
    public Button oneBtn;
    @FXML
    public Button twoBtn;
    @FXML
    public Button threeBtn;
    @FXML
    public Button fourBtn;
    @FXML
    public Button fiveBtn;
    @FXML
    public Button sixBtn;
    @FXML
    public Button sevenBtn;
    @FXML
    public Button eightBtn;
    @FXML
    public Button nineBtn;
    @FXML
    public Button delBtn;
    @FXML
    public Button zeroBtn;
    @FXML
    public Button dotBtn;

    private List<JSONObject> branches;

    private double width;
    private double height;

    public LoginController() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.width = screenSize.getWidth();
        this.height = screenSize.getHeight();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.setLayout();

        loginTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loginTextField.requestFocus();
            }
        });

        loginTextField.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    loginEmployee(ke);
                }
                if (ke.getCode().equals(KeyCode.TAB)) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            branchBox.requestFocus();
                        }
                    });
                }
            }
        });

        branchBox.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    loginEmployee(ke);
                }
            }
        });
        removeImage.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            this.loginTextField.setText("");
        });
        if (App.appContextHolder.isOnlineMode()) {
            offlineLbl.setVisible(false);
            givePointsBtn.setVisible(false);
            reconnectBtn.setVisible(false);
        } else {
            loginTextField.setVisible(false);
            loginBtn.setVisible(false);
            branchBox.setVisible(false);
        }
        givePointsBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
          try {
              Stage givePointsStage = new Stage();
              Parent root = FXMLLoader.load(App.class.getResource(AppConfigConstants.GIVE_POINTS_FXML));
              givePointsStage.setScene(new Scene(root, 400,220));

              givePointsStage.setTitle("Rush POS Sync");
              givePointsStage.resizableProperty().setValue(Boolean.FALSE);
              givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
              givePointsStage.show();
              ((Stage) branchBox.getScene().getWindow()).close();
          } catch (IOException e) {
              e.printStackTrace();
          }
        });

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

        reconnectBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            try {
                branchBox.getItems().clear();
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
                offlineLbl.setVisible(false);
                givePointsBtn.setVisible(false);
                reconnectBtn.setVisible(false);
                loginTextField.setVisible(true);
                loginBtn.setVisible(true);
                branchBox.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
                prompt("Unable to connect to Rush Server due to network connection problem. Please check your internet connection and try again.", event);
                offlineLbl.setVisible(true);
                givePointsBtn.setVisible(true);
                reconnectBtn.setVisible(true);
                loginTextField.setVisible(false);
                loginBtn.setVisible(false);
                branchBox.setVisible(false);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });



        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            loginEmployee(event);
        });
    }

    private void loginEmployee(InputEvent event) {
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
                    App.appContextHolder.setBranchId(branchId);
                    //Redirect to home page

                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Browser(),width - 20,height - 70, javafx.scene.paint.Color.web("#666970")));
                    stage.setTitle("Rush POS Sync");
                    stage.setMaximized(true);
                    stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
                    stage.show();
                    App.appContextHolder.setHomeStage(stage);
                    ((Stage) branchBox.getScene().getWindow()).close();
                } else if (jsonObject.get("error_code").equals("0x2")) {
                    showRequirePinModal(event);
                } else {
                    prompt((String) jsonObject.get("message"), event);
                }

            } catch (IOException e) {

                jsonResponse = null;
                App.appContextHolder.setOnlineMode(false);
                prompt("Unable to connect to Rush Server due to network connection problem. Please check your internet connection and try again.", event);
                offlineLbl.setVisible(true);
                givePointsBtn.setVisible(true);
                reconnectBtn.setVisible(true);
                loginTextField.setVisible(false);
                loginBtn.setVisible(false);
                branchBox.setVisible(false);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            prompt("Unable to connect to Rush Server due to network connection problem. Please check your internet connection and try again.", event);
            offlineLbl.setVisible(true);
            givePointsBtn.setVisible(true);
            reconnectBtn.setVisible(true);
            loginTextField.setVisible(false);
            loginBtn.setVisible(false);
            branchBox.setVisible(false);
        }
    }

    private void showRequirePinModal(InputEvent event) {
        try {

            String branchId = "";
            for (JSONObject branch : branches) {
                if (branchBox.getSelectionModel().getSelectedItem().equals(branch.get("name"))) {
                    branchId = (String) branch.get("id");
                }
            }


            Stage stage = new Stage();
            FXMLLoader  loader  = new FXMLLoader(App.class.getResource("/app/fxml/require-pin.fxml"));
            RequirePinController controller = new RequirePinController(overlayPane, loginTextField.getText(), branchId );
            loader.setController(controller);
            stage.setScene(new Scene(loader.load(), 900,600));
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

    private void prompt(String message, InputEvent event) {
        overlayPane.setVisible(true);
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle("Rush POS Sync");
        alert.initStyle(StageStyle.UTILITY);
        alert.initOwner(
                ((Node)event.getSource()).getScene().getWindow() );

        alert.setOnCloseRequest((DialogEvent e) -> {
            overlayPane.setVisible(false);
        });
        alert.setHeaderText("LOGIN FAILED");

        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            overlayPane.setVisible(false);
        }

       /* try {
            overlayPane.setVisible(true);
            Stage stage = new Stage();
            FXMLLoader  loader  = new FXMLLoader(App.class.getResource("/app/fxml/custom-dialog.fxml"));
            CustomDialogController controller = new CustomDialogController(message, overlayPane);
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
        }*/
    }

    private void setLayout() {

        mainPane.setPrefWidth(width);
        mainPane.setPrefHeight(height);
        rightPane.setPrefHeight(height);
        rightPane.setPrefWidth(width / 2);
        rightPane.setLayoutX(width / 2);
        overlayPane.setPrefHeight(height);
        overlayPane.setPrefWidth(width);

        branchBox.setLayoutX((width / 2) / 5);
        branchBox.setPrefWidth(branchBox.getLayoutX() * 3);
        loginBtn.setLayoutX((width / 2) / 5);
        loginBtn.setPrefWidth(loginBtn.getLayoutX() * 3);
        loginTextField.setLayoutX((width / 2) / 5);
        loginTextField.setPrefWidth(loginBtn.getLayoutX() * 3);
        givePointsBtn.setLayoutX((width / 2) / 5);
        givePointsBtn.setPrefWidth(givePointsBtn.getLayoutX() * 3);
        reconnectBtn.setLayoutX((width / 2) / 5);
        reconnectBtn.setPrefWidth(reconnectBtn.getLayoutX() * 3);
        offlineLbl.setLayoutX(loginBtn.getLayoutX() + loginBtn.getPrefWidth() / 3);
        offlineLbl.setPrefWidth(loginBtn.getLayoutX() * 4);
        rushLogo.setLayoutX(((width / 2) / 5) * 1.5);
        rushLogo.setFitWidth(((width / 2) / 5) * 2);
        employeeLbl.setLayoutX(loginBtn.getLayoutX() + loginBtn.getPrefWidth() / 5);

        double numberBtnWidth = rightPane.getPrefWidth() / 5;
        oneBtn.setPrefWidth(numberBtnWidth);
        oneBtn.setPrefHeight(numberBtnWidth);
        oneBtn.setLayoutX(numberBtnWidth / 4);
        twoBtn.setPrefWidth(numberBtnWidth);
        twoBtn.setPrefHeight(numberBtnWidth);
        twoBtn.setLayoutX(oneBtn.getLayoutX() + oneBtn.getPrefWidth() + numberBtnWidth / 4);
        threeBtn.setPrefWidth(numberBtnWidth);
        threeBtn.setPrefHeight(numberBtnWidth);
        threeBtn.setLayoutX(twoBtn.getLayoutX() + twoBtn.getPrefWidth() + numberBtnWidth / 4);
        fourBtn.setPrefWidth(numberBtnWidth);
        fourBtn.setPrefHeight(numberBtnWidth);
        fourBtn.setLayoutX(numberBtnWidth / 4);
        fiveBtn.setPrefWidth(numberBtnWidth);
        fiveBtn.setPrefHeight(numberBtnWidth);
        fiveBtn.setLayoutX(oneBtn.getLayoutX() + oneBtn.getPrefWidth() + numberBtnWidth / 4);
        sixBtn.setPrefWidth(numberBtnWidth);
        sixBtn.setPrefHeight(numberBtnWidth);
        sixBtn.setLayoutX(twoBtn.getLayoutX() + twoBtn.getPrefWidth() + numberBtnWidth / 4);
        sevenBtn.setPrefWidth(numberBtnWidth);
        sevenBtn.setPrefHeight(numberBtnWidth);
        sevenBtn.setLayoutX(numberBtnWidth / 4);
        eightBtn.setPrefWidth(numberBtnWidth);
        eightBtn.setPrefHeight(numberBtnWidth);
        eightBtn.setLayoutX(oneBtn.getLayoutX() + oneBtn.getPrefWidth() + numberBtnWidth / 4);
        nineBtn.setPrefWidth(numberBtnWidth);
        nineBtn.setPrefHeight(numberBtnWidth);
        nineBtn.setLayoutX(twoBtn.getLayoutX() + twoBtn.getPrefWidth() + numberBtnWidth / 4);
        delBtn.setPrefWidth(numberBtnWidth);
        delBtn.setPrefHeight(numberBtnWidth);
        delBtn.setLayoutX(numberBtnWidth / 4);
        zeroBtn.setPrefWidth(numberBtnWidth);
        zeroBtn.setPrefHeight(numberBtnWidth);
        zeroBtn.setLayoutX(oneBtn.getLayoutX() + oneBtn.getPrefWidth() + numberBtnWidth / 4);
        dotBtn.setPrefWidth(numberBtnWidth);
        dotBtn.setPrefHeight(numberBtnWidth);
        dotBtn.setLayoutX(twoBtn.getLayoutX() + twoBtn.getPrefWidth() + numberBtnWidth / 4);
        removeImage.setLayoutX(numberBtnWidth / 5 + (numberBtnWidth/2.5));
        removeImage.setLayoutY(delBtn.getLayoutY() + (numberBtnWidth/ 2.5));
    }


}
