package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ComicDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("slug")
    private String slug;
    @SerializedName("title")
    private String title;
    @SerializedName("cover_image")
    private String coverImage;
    // TODO: add other fields as needed

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
}

