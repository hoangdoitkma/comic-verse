package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ChapterItemDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("access_type")
    private String accessType;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAccessType() { return accessType; }
}

