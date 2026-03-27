package com.example.comicversev1.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request body cho POST /comics/reading-history-info
 */
public class ReadingHistoryInfoRequest {
    @SerializedName("items")
    private List<Item> items;

    public ReadingHistoryInfoRequest(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        @SerializedName("comicId")
        public int comicId;
        @SerializedName("chapterId")
        public int chapterId;
        @SerializedName("pageIndex")
        public int pageIndex;

        public Item(int comicId, int chapterId, int pageIndex) {
            this.comicId = comicId;
            this.chapterId = chapterId;
            this.pageIndex = pageIndex;
        }
    }
}
