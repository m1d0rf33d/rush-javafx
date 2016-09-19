package com.yondu;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

/**
 * Created by aomine on 9/19/16.
 */
public class Browser extends Region {

    private static final String INDEX_PAGE_PATH = "/index.html";

    final WebView webView = new WebView();
    final WebEngine webEngine = webView.getEngine();

    public Browser() {
        //Retrieve local html resource
        String indexPage = App.class.getResource(INDEX_PAGE_PATH).toExternalForm();
        // Add a Java callback object to a WebEngine document once it has loaded.
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {

                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("indexBridge", new IndexBridge(webEngine));
                    }
                }
        );

        webEngine.load(indexPage);
        getChildren().add(webView);
    }

    //Default configs
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(webView,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(double height) {
        return 750;
    }

    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}
