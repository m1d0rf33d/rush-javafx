package com.yondu.utils;

/**
 * Created by aomine on 9/27/16.
 */
public class FieldValidator {

    private FieldValidator() {

    }

    public static String isValidString(String value, String fieldName) {
        if (value == null || value.equals("")) {
            return fieldName + " is required. \n";
        }
        return "";
    }

}
