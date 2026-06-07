package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class HotSearchDTO {
    @SerializedName("keyword")
    public String keyword;

    @SerializedName("contentType")
    public String contentType;

    @SerializedName("searchCount")
    public long searchCount;

    @SerializedName("lastSearchedAtMillis")
    public long lastSearchedAtMillis;
}
