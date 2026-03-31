package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChapterDetailDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("chapterNumber")
    private float chapterNum;
    @SerializedName("pages")
    private List<String> images;
    @SerializedName("content")
    private String content;
    @SerializedName("nextChapterId")
    private Integer nextChapterId;
    @SerializedName("prevChapterId")
    private Integer prevChapterId;
    @SerializedName("totalPages")
    private int totalPages;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public float getChapterNum() { return chapterNum; }
    public List<String> getImages() { return images; }
    public String getContent() { return content; }
    public Integer getNextChapterId() { return nextChapterId; }
    public Integer getPrevChapterId() { return prevChapterId; }
    public int getTotalPages() { return totalPages; }
}
