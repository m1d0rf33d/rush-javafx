package com.yondu;

import javafx.scene.web.WebEngine;

/**
 * Created by aomine on 9/19/16.
 */
public class IndexBridge {

    private WebEngine webEngine;

    public IndexBridge(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public void login() {
        //Call rush api
        String x = "";
        webEngine.executeScript("loginFailed()");
    }


}
