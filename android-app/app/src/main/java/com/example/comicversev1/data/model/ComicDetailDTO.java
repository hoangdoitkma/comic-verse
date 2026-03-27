package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ComicDetailDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("slug")
    private String slug;
    @SerializedName("title")
    private String title;
    @SerializedName("thumbnailUrl")
    private String coverImage;
    @SerializedName("ai_summary")
    private String aiSummary;
    @SerializedName("chapters")
    private List<ChapterItemDTO> chapters;

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
    public String getAiSummary() { return aiSummary; }
    public List<ChapterItemDTO> getChapters() { return chapters; }
}

