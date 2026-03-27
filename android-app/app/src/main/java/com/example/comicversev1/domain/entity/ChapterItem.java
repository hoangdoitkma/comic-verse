package com.example.comicversev1.domain.entity;

public class ChapterItem {
    private final int id;
    private final String title;
    private final String accessType;

    public ChapterItem(int id, String title, String accessType) {
        this.id = id;
        this.title = title;
        this.accessType = accessType;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAccessType() { return accessType; }
}

