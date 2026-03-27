package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response item cho POST /comics/reading-history-info
 */
public class ReadingHistoryInfoDTO {
    @SerializedName("comicId")
    public int comicId;

    @SerializedName("title")
    public String title;

    @SerializedName("slug")
    public String slug;

    @SerializedName("coverUrl")
    public String coverUrl;

    @SerializedName("chapterTitle")
    public String chapterTitle;

    @SerializedName("chapterNum")
    public float chapterNum;

    @SerializedName("totalChapters")
    public int totalChapters;

    @SerializedName("progress")
    public int progress;

    @SerializedName("views")
    public long views;

    @SerializedName("likes")
    public long likes;
}
