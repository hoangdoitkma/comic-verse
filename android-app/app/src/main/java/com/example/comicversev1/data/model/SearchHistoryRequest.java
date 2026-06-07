package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class SearchHistoryRequest {
    @SerializedName("keyword")
    public final String keyword;

    @SerializedName("type")
    public final String type;

    public SearchHistoryRequest(String keyword, String type) {
        this.keyword = keyword;
        this.type = type;
    }
}
