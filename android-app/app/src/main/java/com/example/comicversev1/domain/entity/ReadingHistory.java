package com.example.comicversev1.domain.entity;

public class ReadingHistory {
    private final int comicId;
    private final int chapterId;
    private final int pageIndex;
    private final long readAt;

    public ReadingHistory(int comicId, int chapterId, int pageIndex, long readAt) {
        this.comicId = comicId;
        this.chapterId = chapterId;
        this.pageIndex = pageIndex;
        this.readAt = readAt;
    }

    public int getComicId() { return comicId; }
    public int getChapterId() { return chapterId; }
    public int getPageIndex() { return pageIndex; }
    public long getReadAt() { return readAt; }
}

