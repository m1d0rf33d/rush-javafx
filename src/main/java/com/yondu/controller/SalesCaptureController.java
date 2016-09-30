package com.yondu.controller;

import com.yondu.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import static com.yondu.model.AppConfigConstants.*;

/** JavaFx controller mapped to sales-capture.fxml
 *
 *  @m1d0rf33d
 */
public class SalesCaptureController {

    @FXML
    public Button previewButton;

    private Stage resultStage;

    public void captureSalesArea() {
        Stage stage = (Stage) this.previewButton.getScene().getWindow();
        Double salesPosX = stage.getX(),
                salesPosY = stage.getY(),
                salesWidth = stage.getWidth(),
                salesHeight = stage.getHeight();

        App.appContextHolder.setSalesPosX(salesPosX.intValue());
        App.appContextHolder.setSalesPosY(salesPosY.intValue());
        App.appContextHolder.setSalesWidth(salesWidth.intValue());
        App.appContextHolder.setSalesHeight(salesHeight.intValue());

        Stage resultStage = new Stage();
        Parent root = null;
        try {
            root = FXMLLoader.load(App.class.getResource(CAPTURE_RESULT_FXML));
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultStage.setScene(new Scene(root, 300,200));
        resultStage.setX(600);
        resultStage.setY(200);
        resultStage.resizableProperty().setValue(false);
        resultStage.show();

        ((Stage) this.previewButton.getScene().getWindow()).close();
    }
}
