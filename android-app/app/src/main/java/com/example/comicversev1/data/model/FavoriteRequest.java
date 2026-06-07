package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class FavoriteRequest {
    @SerializedName("slug")
    public final String slug;

    public FavoriteRequest(String slug) {
        this.slug = slug;
    }
}
