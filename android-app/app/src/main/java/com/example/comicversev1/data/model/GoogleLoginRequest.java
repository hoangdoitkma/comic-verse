package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {
    @SerializedName("idToken")
    private final String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}
