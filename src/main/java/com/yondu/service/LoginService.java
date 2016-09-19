package com.yondu.service;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by aomine on 9/19/16.
 */
public class LoginService  implements Initializable {


    public List<String> getBranches() {
        ArrayList<String> data = new ArrayList<>();
        data.add("branch 1");
        data.add("branch 2");
        return data;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
