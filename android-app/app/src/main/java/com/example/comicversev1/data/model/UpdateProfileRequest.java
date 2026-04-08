package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("displayName")
    public String displayName;

    public UpdateProfileRequest(String displayName) {
        this.displayName = displayName;
    }
}
