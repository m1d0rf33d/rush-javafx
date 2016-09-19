package com.yondu.model;

/**
 * Created by aomine on 9/19/16.
 */
public class ApiResponse<T> {
    private int status;
    private T data;
    private T error_code;

    public T getError_code() {
        return error_code;
    }

    public void setError_code(T error_code) {
        this.error_code = error_code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
