package com.yondu.service;

import com.yondu.App;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.yondu.model.constants.AppConfigConstants.MEMBER_INQUIRY_SCREEN;

/**
 * Created by lynx on 2/22/17.
 */
public class CommonService {

    private RouteService routeService = new RouteService();

    public void exitMember() {
        App.appContextHolder.setCustomerUUID(null);
        App.appContextHolder.setCustomerMobile(null);
        routeService.loadContentPage(App.appContextHolder.getRootStackPane(), MEMBER_INQUIRY_SCREEN);

        Button memberInquiryButton = (Button) App.appContextHolder.getRootVBox().getScene().lookup("#memberInquiryButton");
        highlight(memberInquiryButton);
    }

    public void highlight (Button button) {
        VBox sideBarVBox = (VBox) App.appContextHolder.getRootVBox().getScene().lookup("#sideBarVBox");
        List<Node> nodes = sideBarVBox.getChildren();
        for (Node n : nodes) {
            if (n instanceof Button) {
                Button b = (Button) n;
                b.getStyleClass().remove("sidebar-selected");
            }
        }

        button.getStyleClass().add("sidebar-selected");
    }
}
