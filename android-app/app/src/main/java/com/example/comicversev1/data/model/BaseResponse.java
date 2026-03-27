package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class BaseResponse<T> {
    @SerializedName("status")
    private int status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private T data;
    
    @SerializedName("timestamp")
    private String timestamp;

    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == 200;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

