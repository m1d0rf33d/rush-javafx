package com.yondu.utils;

import com.yondu.App;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Created by erwin on 9/22/2016.
 */
public class ResourcePropertyUtil {


    private ResourcePropertyUtil() {

    }

    public static String getProperty(String propertyFile, String property, Class loadingClass) {
        try {
            Properties prop = new Properties();

            InputStream inputStream = loadingClass.getClassLoader().getResourceAsStream(propertyFile);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propertyFile + "' not found in the classpath");
            }
            return prop.getProperty(property);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return null;
    }
}
