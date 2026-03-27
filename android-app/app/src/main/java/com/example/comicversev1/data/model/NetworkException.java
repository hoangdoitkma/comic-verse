package com.example.comicversev1.data.model;

import java.io.IOException;

public class NetworkException extends IOException {
    private final int errorCode;

    public NetworkException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
