package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("oldPassword")
    public String oldPassword;

    @SerializedName("newPassword")
    public String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
