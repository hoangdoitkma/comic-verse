package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName(value = "token", alternate = {"access_token", "accessToken"})
    private String accessToken;
    @SerializedName(value = "refreshToken", alternate = {"refresh_token"})
    private String refreshToken;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }

    public TokenResponse() {
    }

    public TokenResponse(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
