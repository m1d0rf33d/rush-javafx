package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.Customer;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import com.yondu.utils.PropertyBinder;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/9/17.
 */
public class MemberInquiryController implements Initializable {

    @FXML
    public TextField mobileTextField;
    @FXML
    public Button viewMemberButton;

    private MemberDetailsService memberDetailsService = new MemberDetailsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewMemberButton.setOnMouseClicked((MouseEvent e) -> {

            App.appContextHolder.getRootVBox().setOpacity(.50);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(true);
            }
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.5)
            );
            pause.setOnFinished(event -> {

                ApiResponse apiResponse = memberDetailsService.loginCustomer(mobileTextField.getText());
                if (apiResponse.isSuccess()) {
                    try {
                        VBox bodyStackPane = App.appContextHolder.getRootStackPane();
                        bodyStackPane.getChildren().clear();
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MEMBER_DETAILS_SCREEN));
                        Parent root = fxmlLoader.load();
                        bodyStackPane.getChildren().add(root);
                        MemberDetailsController memberDetailsController = fxmlLoader.getController();
                        memberDetailsController.setCustomer((Customer) apiResponse.getPayload().get("customer"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Text text = new Text(apiResponse.getMessage());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle(AppConfigConstants.APP_TITLE);
                    alert.initStyle(StageStyle.UTILITY);
                    alert.initOwner(viewMemberButton.getScene().getWindow());
                    alert.setHeaderText("REGISTER MEMBER");
                    alert.getDialogPane().setPadding(new Insets(10,10,10,10));
                    alert.getDialogPane().setContent(text);
                    alert.getDialogPane().setPrefWidth(400);
                    alert.show();
                }

                App.appContextHolder.getRootVBox().setOpacity(1);
                for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                    n.setDisable(false);
                }
            });

            pause.play();
        });

        PropertyBinder.bindNumberOnly(mobileTextField);
        PropertyBinder.bindMaxLength(11, mobileTextField);
    }

}
