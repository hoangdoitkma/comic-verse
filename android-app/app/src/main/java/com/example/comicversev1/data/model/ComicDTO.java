package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ComicDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("slug")
    private String slug;
    @SerializedName("title")
    private String title;
    @SerializedName("thumbnailUrl")
    private String coverImage;
    @SerializedName("viewCount")
    private long viewCount;
    @SerializedName("totalChapters")
    private int totalChapters;

    @SerializedName("accessType")
    private String accessType;

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
    public long getViewCount() { return viewCount; }
    public int getTotalChapters() { return totalChapters; }
    public String getAccessType() { return accessType; }
}

