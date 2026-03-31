package com.example.comicversev1.presentation.novel.reader;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NovelChapterData {
    @SerializedName("chapter_title")
    private String chapterTitle;
    
    @SerializedName("content")
    private List<String> content;

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
