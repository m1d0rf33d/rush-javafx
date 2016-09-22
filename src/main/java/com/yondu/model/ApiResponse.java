package com.yondu.model;

import java.util.LinkedHashMap;

/**
 * Created by aomine on 9/19/16.
 */
public class ApiResponse<T> {
    private T data;
    private String error_code;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }
}
