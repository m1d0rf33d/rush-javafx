package com.yondu.controller;

import java.io.*;
import java.util.Properties;

import static com.yondu.model.constants.AppConfigConstants.DIVIDER;
import static com.yondu.model.constants.AppConfigConstants.OCR_CONFIG;
import static com.yondu.model.constants.AppConfigConstants.RUSH_HOME;

/**
 * Created by aomine on 3/23/17.
 */
public class BaseController {

    public Boolean readOcrConfig() {


        try {

            File file = new File(RUSH_HOME + DIVIDER + OCR_CONFIG);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    PrintWriter fstream = new PrintWriter(new FileWriter(file));
                    fstream.println("enabled=" + "on");
                    fstream.flush();
                    fstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            Properties prop = new Properties();
            InputStream inputStream = new FileInputStream(new File(RUSH_HOME + DIVIDER + OCR_CONFIG));
            prop.load(inputStream);
            String str= prop.getProperty("enabled");
            if (str.equals("on")) {
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
