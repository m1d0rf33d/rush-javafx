package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.ApiResponse;
import com.yondu.model.constants.AppConfigConstants;
import com.yondu.model.constants.AppState;
import com.yondu.service.MemberDetailsService;
import com.yondu.service.MenuService;
import com.yondu.service.NotificationService;
import com.yondu.service.RouteService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.*;

/**
 * Created by lynx on 2/8/17.
 */
public class MobileLoginController implements Initializable {

    @FXML
    public Button cancelButton;
    @FXML
    public Button submitButton;
    @FXML
    public TextField mobileTextField;
    @FXML
    public Label errorLabel;


    private MemberDetailsService memberDetailsService = new MemberDetailsService();
    private RouteService routeService = new RouteService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        errorLabel.setVisible(false);

        cancelButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            App.appContextHolder.getRootVBox().setOpacity(1);
            for (Node n : App.appContextHolder.getRootVBox().getChildren()) {
                n.setDisable(false);
            }
            ((Stage) cancelButton.getScene().getWindow()).close();
        });

        submitButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            ((Stage) submitButton.getScene().getWindow()).close();
            PauseTransition pause = new PauseTransition(
                    Duration.seconds(.5)
            );
            pause.setOnFinished(event -> {
                ApiResponse apiResponse = memberDetailsService.loginCustomer(mobileTextField.getText());
                if (apiResponse.isSuccess()) {
                    AppState state = App.appContextHolder.getAppState();
                    if (state.equals(AppState.EARN_POINTS)) {
                        routeService.loadEarnPointsScreen();
                    } else if (state.equals(AppState.REDEEM_REWARDS)) {
                        routeService.loadRedeemRewardsScreen();
                    } else if (state.equals(AppState.ISSUE_REWARDS)) {
                        routeService.loadIssueRewardsScreen();
                    } else if (state.equals(AppState.PAY_WITH_POINTS)) {
                        routeService.loadPayWithPoints();
                    } else if (state.equals(AppState.TRANSACTIONS)) {
                        routeService.loadTransactionsScreen();
                    }
                } else {
                    App.appContextHolder.getRootVBox().setOpacity(1);
                    for (Node n :  App.appContextHolder.getRootVBox().getChildren()) {
                        n.setDisable(false);
                    }

                    Text text = new Text(apiResponse.getMessage());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle(AppConfigConstants.APP_TITLE);
                    alert.initStyle(StageStyle.UTILITY);
                    alert.initOwner(App.appContextHolder.getRootVBox().getScene().getWindow());
                    alert.setHeaderText("LOGIN MEMBER");
                    alert.getDialogPane().setPadding(new Insets(10,10,10,10));
                    alert.getDialogPane().setContent(text);
                    alert.getDialogPane().setPrefWidth(400);
                    alert.show();
                }
            });
            pause.play();

        });

    }

}
