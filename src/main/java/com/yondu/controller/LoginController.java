package com.yondu.controller;

import com.yondu.App;
import com.yondu.model.constants.AppState;
import com.yondu.service.LoginService;
import com.yondu.utils.PropertyBinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

import static com.yondu.model.constants.AppConfigConstants.DIVIDER;


/**
 * Created by lynx on 2/6/17.
 */
public class LoginController implements Initializable {

    @FXML
    public ImageView rushLogoImageView;
    @FXML
    public ImageView removeImageView;
    @FXML
    public StackPane bodyStackPane;
    @FXML
    public Button oneButton;
    @FXML
    public Button twoButton;
    @FXML
    public Button threeButton;
    @FXML
    public Button fourButton;
    @FXML
    public Button fiveButton;
    @FXML
    public Button sixButton;
    @FXML
    public Button sevenButton;
    @FXML
    public Button eightButton;
    @FXML
    public Button nineButton;
    @FXML
    public Button zeroButton;
    @FXML
    public Button dotButton;
    @FXML
    public Button removeButton;
    @FXML
    public VBox rootVBox;
    @FXML
    public ScrollPane rootScrollPane;

    private LoginService loginService = App.appContextHolder.loginService;

    public LoginController() {
        if (System.getProperty("os.name").contains("Windows")) {
            DIVIDER = "\\";
        } else {
            DIVIDER = "//";
        }
    }

    public void initAfterLoad() {
        App.appContextHolder.setCurrentState(AppState.LOGIN);
        App.appContextHolder.setRootContainer(rootVBox);
        rootScrollPane.setFitToHeight(true);
        rootScrollPane.setFitToWidth(true);

        removeImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/remove.png").toExternalForm()));
        rushLogoImageView.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/rush_logo.png").toExternalForm()));

        ImageView img = new ImageView();
        img.setImage(new javafx.scene.image.Image(App.class.getResource("/app/images/loading.gif").toExternalForm()));
        img.setFitWidth(150);
        img.setFitHeight(150);
        img.getStyleClass().add("loading-img");
        bodyStackPane.getChildren().clear();
        bodyStackPane.getChildren().addAll(img);
        bodyStackPane.setPadding(new Insets(30,0,0,0));

        PropertyBinder.setNumberButtonClick(oneButton, "1");
        PropertyBinder.setNumberButtonClick(twoButton, "2");
        PropertyBinder.setNumberButtonClick(threeButton, "3");
        PropertyBinder.setNumberButtonClick(fourButton, "4");
        PropertyBinder.setNumberButtonClick(fiveButton, "5");
        PropertyBinder.setNumberButtonClick(sixButton, "6");
        PropertyBinder.setNumberButtonClick(sevenButton, "7");
        PropertyBinder.setNumberButtonClick(eightButton, "8");
        PropertyBinder.setNumberButtonClick(nineButton, "9");
        PropertyBinder.setNumberButtonClick(zeroButton, "0");
        PropertyBinder.setNumberButtonClick(dotButton, ".");

        removeButton.setOnMouseClicked((MouseEvent e) -> {
            TextField loginTextField = (TextField) rootVBox.getScene().lookup("#loginTextField");
            if (loginTextField.getText() != null) {
                String subStr = loginTextField.getText().substring(0, loginTextField.getText().length() -1);
                loginTextField.setText(subStr);
            }
        });
  /*      ReportService reportService = new ReportService();
      try {
            reportService.exportXls();
        } catch (JRException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        loginService.initialize();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
