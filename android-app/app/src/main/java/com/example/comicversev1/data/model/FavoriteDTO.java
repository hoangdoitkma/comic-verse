package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class FavoriteDTO {
    @SerializedName("comicId")
    public int comicId;

    @SerializedName("slug")
    public String slug;

    @SerializedName("title")
    public String title;

    @SerializedName("thumbnailUrl")
    public String thumbnailUrl;

    @SerializedName("contentType")
    public String contentType;

    @SerializedName("addedAtMillis")
    public long addedAtMillis;
}
