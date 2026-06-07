package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class SearchHistoryItemDTO {
    @SerializedName("keyword")
    public String keyword;

    @SerializedName("contentType")
    public String contentType;

    @SerializedName("searchedAtMillis")
    public long searchedAtMillis;
}
