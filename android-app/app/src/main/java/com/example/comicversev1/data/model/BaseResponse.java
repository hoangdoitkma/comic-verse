package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> {
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private T data;
    @SerializedName("error_code")
    private String errorCode;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

