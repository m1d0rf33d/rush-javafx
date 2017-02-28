package com.yondu.controller;

import com.sun.javafx.scene.control.skin.FXVK;
import com.yondu.App;
import com.yondu.AppContextHolder;
import com.yondu.model.Branch;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.yondu.AppContextHolder.*;
import static com.yondu.model.constants.AppConfigConstants.*;
/**
 * Created by lynx on 2/24/17.
 */
public class LoginOnlineController implements Initializable{

    @FXML
    public ComboBox branchComboBox;
    @FXML
    public Button loginButton;
    @FXML
    public TextField loginTextField;

    private List<Branch> branches;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Event handlers for clickable nodes
        loginTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (newPropertyValue)
                    FXVK.detach();
            }

        });
        PropertyBinder.bindNumberWitDot(loginTextField);

        loginButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {

            App.appContextHolder.getLoginHBox().setOpacity(.50);
            for (Node n : App.appContextHolder.getLoginHBox().getChildren()) {
                n.setDisable(true);
            }
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.5)
            );
            pause.setOnFinished(event -> {
                loginEmployee();
                App.appContextHolder.getLoginHBox().setOpacity(1);
                for (Node n : App.appContextHolder.getLoginHBox().getChildren()) {
                    n.setDisable(false);
                }
            });
            pause.play();

        });

    }
    private void loginEmployee() {

        String username = this.loginTextField.getText();
        String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();
        String branchId = "";
        for (Branch branch : branches) {
            if (branch.getName().equals(branchName)) {
                branchId = branch.getId();
                break;
            }
        }

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("employee_id", username));
        params.add(new BasicNameValuePair("branch_id", branchId));
        String url = BASE_URL + LOGIN_ENDPOINT;
        JSONObject jsonObject = App.appContextHolder.getApiService().call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
        if (jsonObject != null) {
            if (jsonObject.get("error_code").equals("0x0")) {
                JSONObject data = (JSONObject) jsonObject.get("data");
                App.appContextHolder.setEmployeeName(((String) data.get("name")));
                App.appContextHolder.setEmployeeId((String) data.get("id"));
                App.appContextHolder.setBranchId(branchId);
                App.appContextHolder.setBranchName(branchName);
                App.appContextHolder.getRouteService().goToMenuScreen((Stage) branchComboBox.getScene().getWindow());
            } else if (jsonObject.get("error_code").equals("0x2")) {
                showPinDialog();
            } else {
                Text text = new Text((String) jsonObject.get("message"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setTitle(AppConfigConstants.APP_TITLE);
                alert.initStyle(StageStyle.UTILITY);
                alert.initOwner(loginButton.getScene().getWindow());
                alert.setHeaderText("LOGIN");
                alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
                alert.getDialogPane().setContent(text);
                alert.getDialogPane().setPrefWidth(400);
                alert.show();
            }
        } else {
            loadOffline();
        }
    }
    private void showPinDialog() {
        try {
            disableMenu();

            String username = this.loginTextField.getText();
            String branchName = (String) branchComboBox.getSelectionModel().getSelectedItem();
            String branchId = "";
            for (Branch branch : branches) {
                if (branch.getName().equals(branchName)) {
                    branchId = (String) branch.getId();
                    break;
                }
            }

            HBox rootHBox = (HBox) loginTextField.getScene().lookup("#rootHBox");
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(PIN_SCREEN));
            Parent root = fxmlLoader.load();
            PinController controller = fxmlLoader.getController();
            controller.setRootHBox(rootHBox);
            controller.setLogin(username);
            controller.setBranchId(branchId);
            Scene scene = new Scene(root, 500,300);
            stage.setScene(scene);
            stage.setTitle(APP_TITLE);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            stage.initOwner(rootHBox.getScene().getWindow());
            stage.setOnCloseRequest(new javafx.event.EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    enableMenu();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disableMenu() {
        HBox rootHBox = (HBox) loginTextField.getScene().lookup("#rootHBox");
        for (Node n : rootHBox.getChildren()) {
            n.setDisable(true);
        }
    }
    public void enableMenu() {
        HBox rootHBox = (HBox) loginTextField.getScene().lookup("#rootHBox");
        for (Node n : rootHBox.getChildren()) {
            n.setDisable(false);
        }
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
        for (Branch branch : branches) {
            branchComboBox.getItems().add(branch.getName());
        }
        branchComboBox.getSelectionModel().selectFirst();
    }

    private void loadOffline() {
        try {
            Text text = new Text("Network connection error.");
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle(AppConfigConstants.APP_TITLE);
            alert.initStyle(StageStyle.UTILITY);
            alert.initOwner(loginButton.getScene().getWindow());
            alert.setHeaderText("LOGIN");
            alert.getDialogPane().setPadding(new javafx.geometry.Insets(10,10,10,10));
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPrefWidth(400);
            alert.show();

            VBox numbersVBox = (VBox) App.appContextHolder.getLoginHBox().getScene().lookup("#numbersVBox");
            numbersVBox.setVisible(false);
            StackPane bodyStackPane = (StackPane) App.appContextHolder.getLoginHBox().getScene().lookup("#bodyStackPane");
            bodyStackPane.getChildren().clear();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfigConstants.LOGIN_OFFLINE_FXML));
            Parent root = fxmlLoader.load();
            bodyStackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
