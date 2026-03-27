package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ReadingHistoryRequest {
    @SerializedName("comic_id")
    private final int comicId;
    @SerializedName("chapter_id")
    private final int chapterId;
    @SerializedName("page_index")
    private final int pageIndex;

    public ReadingHistoryRequest(int comicId, int chapterId, int pageIndex) {
        this.comicId = comicId;
        this.chapterId = chapterId;
        this.pageIndex = pageIndex;
    }
}

