package com.yondu;

import com.yondu.service.HomeService;
import com.yondu.service.JavaFXVirtualKeyboard;
import com.yondu.service.LoginService;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLInputElement;

import java.io.*;
import java.util.List;


public class Browser extends Region{

    private static final String LOGIN_PAGE = "/app/login.html";

    final WebView webView     = new WebView();
    final WebEngine webEngine = webView.getEngine();

    public Browser() {
        //Retrieve local html resource
        String page = App.class.getResource("/app/home.html").toExternalForm();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                Java2JavascriptUtils.connectBackendObject(webEngine, "loginService", new LoginService(webEngine));
                Java2JavascriptUtils.connectBackendObject(webEngine, "homeService", new HomeService(webEngine, webView));
            }
        });
        webEngine.load(page);
        getChildren().add(webView);
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
