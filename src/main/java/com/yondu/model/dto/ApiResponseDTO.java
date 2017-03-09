package com.yondu.model.dto;

/**
 * Created by aomine on 3/9/17.
 */
public class ApiResponseDTO<T> {
    private T data;
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
