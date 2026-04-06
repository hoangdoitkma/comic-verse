package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

public class ReadingHistoryRequest {
    @SerializedName("comicId")
    private final int comicId;
    @SerializedName("chapterId")
    private final int chapterId;
    @SerializedName("lastPage")
    private final int pageIndex;

    public ReadingHistoryRequest(int comicId, int chapterId, int pageIndex) {
        this.comicId = comicId;
        this.chapterId = chapterId;
        this.pageIndex = pageIndex;
    }
}

