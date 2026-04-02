package com.example.comicversev1.domain.entity;

public class ComicEntity {
    private final int id;
    private final String slug;
    private final String title;
    private final String coverImage;
    private final String accessType;

    public ComicEntity(int id, String slug, String title, String coverImage, String accessType) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.coverImage = coverImage;
        this.accessType = accessType;
    }

    public int getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getCoverImage() { return coverImage; }
    public String getAccessType() { return accessType; }
}

