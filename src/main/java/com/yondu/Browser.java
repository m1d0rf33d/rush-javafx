package com.yondu;

import com.yondu.service.HomeService;
import com.yondu.service.LoginService;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.simple.JSONObject;

import java.io.*;


public class Browser extends Region{

    private static final String LOGIN_PAGE = "/app/login.html";

    final WebView webView     = new WebView();
    final WebEngine webEngine = webView.getEngine();

    public Browser() {
        //Retrieve local html resource
        String indexPage = App.class.getResource(LOGIN_PAGE).toExternalForm();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                Java2JavascriptUtils.connectBackendObject(webEngine, "loginService", new LoginService(webEngine));
                Java2JavascriptUtils.connectBackendObject(webEngine, "homeService", new HomeService(webEngine));
              /*  JSObject jsobj = (JSObject) webEngine.executeScript("window");
                jsobj.setMember("java", new LoginService(webEngine));*/

            }
        });
        webEngine.load(indexPage);
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
