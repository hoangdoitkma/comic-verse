package com.example.comicversev1.data.model;

public class ViewTrackingRequest {
    private int comicId;
    private int chapterId;

    public ViewTrackingRequest(int comicId, int chapterId) {
        this.comicId = comicId;
        this.chapterId = chapterId;
    }

    public int getComicId() {
        return comicId;
    }

    public void setComicId(int comicId) {
        this.comicId = comicId;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }
}
