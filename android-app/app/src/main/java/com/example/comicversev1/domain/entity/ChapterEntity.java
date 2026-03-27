package com.example.comicversev1.domain.entity;

import java.util.List;

public class ChapterEntity {
    private final int id;
    private final String title;
    private final float chapterNum;
    private final List<String> images;
    private final Integer nextChapterId;
    private final Integer prevChapterId;

    public ChapterEntity(int id, String title, float chapterNum, List<String> images,
                         Integer nextChapterId, Integer prevChapterId) {
        this.id = id;
        this.title = title;
        this.chapterNum = chapterNum;
        this.images = images;
        this.nextChapterId = nextChapterId;
        this.prevChapterId = prevChapterId;
    }

    // Backward-compatible constructor
    public ChapterEntity(int id, String title, List<String> images) {
        this(id, title, 0, images, null, null);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public float getChapterNum() { return chapterNum; }
    public List<String> getImages() { return images; }
    public Integer getNextChapterId() { return nextChapterId; }
    public Integer getPrevChapterId() { return prevChapterId; }
}
