package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;
    
    @SerializedName("displayName")
    private String displayName;
    
    @SerializedName("email")
    private String email;

    public String getToken() { return token; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
}

