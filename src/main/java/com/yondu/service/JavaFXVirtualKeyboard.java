package com.yondu.service;

import com.sun.javafx.scene.control.skin.FXVK;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Created by erwin on 10/28/2016.
 */
public class JavaFXVirtualKeyboard {

    private WebView webView;

    public JavaFXVirtualKeyboard(WebView webView) {
        this.webView = webView;
    }

    public void show() {
        FXVK.init(webView);
        FXVK.attach(webView);
    }

    public void hide() {
        FXVK.detach();
    }
}