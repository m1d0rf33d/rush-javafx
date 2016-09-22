package com.yondu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.Account;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.scene.web.WebEngine;

import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

/** Home Module services / Java2Javascript bridge
 *  Methods inside this class can be invoked inside a javascript using alert("__CONNECT__BACKEND__homeService")
 *
 *  @author m1d0rf33d
 */
public class HomeService {

    private WebEngine webEngine;

    public HomeService(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public void loadEmployeeData(final Object callbackfunction) {

        Account account = new Account();
        account.setId("");
        account.setName(App.appContextHolder.getEmployeeName());

        ObjectMapper mapper = new ObjectMapper();
        String data = null;
        try {
            data = mapper.writeValueAsString(account);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        final String jsonData =  data;
        new Thread( () -> {

                runLater( () ->
                        Java2JavascriptUtils.call(callbackfunction, jsonData)
                );

            }
        ).start();
    }
}
