package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ReadingHistorySyncDTO {
    @SerializedName("comicId")
    public int comicId;

    @SerializedName("slug")
    public String slug;

    @SerializedName("title")
    public String title;

    @SerializedName("thumbnailUrl")
    public String thumbnailUrl;

    @SerializedName("authorName")
    public String authorName;

    @SerializedName("viewCount")
    public long viewCount;

    @SerializedName("contentType")
    public String contentType;

    @SerializedName("chapterId")
    public int chapterId;

    @SerializedName("chapterTitle")
    public String chapterTitle;

    @SerializedName("lastPage")
    public int lastPage;

    @SerializedName("updatedAtMillis")
    public long updatedAtMillis;
}
