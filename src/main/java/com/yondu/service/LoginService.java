package com.yondu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yondu.App;
import com.yondu.model.constants.ApiFieldContants;
import com.yondu.model.ApiResponse;
import com.yondu.model.Branch;
import com.yondu.utils.Java2JavascriptUtils;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.yondu.model.constants.AppConfigConstants.GIVE_POINTS_FXML;
import static java.lang.Thread.sleep;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.parse;
import static org.json.simple.JSONValue.toJSONString;

/** Service for Login Module / Java2Javascript Bridge
 *  Methods inside this class can be invoked in javascript using alert("__CONNECT__BACKEND__loginService")
 *
 *  @author m1d0rf33d
 */
public class LoginService {

    private ApiService apiService = new ApiService();
    private WebEngine webEngine;

    public LoginService(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    public void login(String employeeId, String branchId) {
        Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        String jsonResponse = null;
                        try {
                            //Build request body
                            List<NameValuePair> params = new ArrayList<>();
                            params.add(new BasicNameValuePair(ApiFieldContants.EMPLOYEE_ID, employeeId));
                            params.add(new BasicNameValuePair(ApiFieldContants.BRANCH_ID, branchId));
                            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getLoginEndpoint();
                            jsonResponse = apiService.call((url), params, "post", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);

                            JSONParser parser = new JSONParser();
                            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
                            if (jsonObject.get("error_code").equals("0x0")) {
                                JSONObject data = (JSONObject) jsonObject.get("data");
                                App.appContextHolder.setEmployeeName(((String) data.get("name")));
                                App.appContextHolder.setEmployeeId((String) data.get("id"));
                                App.appContextHolder.setBranchId((String) data.get("branch_id"));
                            }

                        } catch (IOException e) {
                            //LOG here
                            jsonResponse = null;
                            App.appContextHolder.setOnlineMode(false);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return jsonResponse;
                    }
                };
            }
        };
        service.setOnSucceeded((WorkerStateEvent e) -> {
            if (e.getSource().getValue() != null) {
                webEngine.executeScript("loginResponseHandler('"+e.getSource().getValue()+"')");
            }

            webEngine.executeScript("closeLoadingModal('"+ App.appContextHolder.isOnlineMode()+"')");
        });
        service.start();

    }

    public void loadBranches(final Object callbackfunction) {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        try {
                            String url = App.appContextHolder.getBaseUrl() + App.appContextHolder.getGetBranchesEndpoint();
                            List<NameValuePair> params = new ArrayList<>();
                            String jsonResponse = apiService.call(url, params, "get", ApiFieldContants.MERCHANT_APP_RESOURCE_OWNER);
                            Thread thread = new Thread(()->{
                                runLater( () ->
                                        Java2JavascriptUtils.call(callbackfunction, jsonResponse)
                                );
                            });
                            thread.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                            App.appContextHolder.setOnlineMode(false);
                        }
                        return null;
                    }
                };
            }
        };
        service.setOnSucceeded((WorkerStateEvent e) -> {
           webEngine.executeScript("closeLoadingModal('"+ App.appContextHolder.isOnlineMode()+"')");
        });
        service.start();

    }
    public void loadGivePointsView() {
        try {
            Stage givePointsStage = new Stage();
            Parent root = FXMLLoader.load(App.class.getResource(GIVE_POINTS_FXML));
            givePointsStage.setScene(new Scene(root, 400,220));
            givePointsStage.setTitle("Rush POS Sync");
            givePointsStage.resizableProperty().setValue(Boolean.FALSE);
            givePointsStage.getIcons().add(new Image(App.class.getResource("/app/images/r_logo.png").toExternalForm()));
            givePointsStage.show();

            App.appContextHolder.getHomeStage().close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
