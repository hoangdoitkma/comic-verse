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

    // Additional fields for Novel Detail
    @SerializedName("synopsis")
    private String synopsis;
    @SerializedName("authorName")
    private String authorName;
    @SerializedName("status")
    private String status;
    @SerializedName("updatedAt")
    private String updatedAt;
    @SerializedName("viewCount")
    private int viewCount;
    @SerializedName("genres")
    private List<String> genres;
    @SerializedName("accessType")
    private String accessType;

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
    public String getAiSummary() { return aiSummary; }
    public List<ChapterItemDTO> getChapters() { return chapters; }
    public String getSynopsis() { return synopsis; }
    public String getAuthorName() { return authorName; }
    public String getStatus() { return status; }
    public String getUpdatedAt() { return updatedAt; }
    public int getViewCount() { return viewCount; }
    public List<String> getGenres() { return genres; }
    public String getAccessType() { return accessType; }
}

