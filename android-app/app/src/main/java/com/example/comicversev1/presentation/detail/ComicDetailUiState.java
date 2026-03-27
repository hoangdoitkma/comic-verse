package com.example.comicversev1.presentation.detail;

import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.List;

public class ComicDetailUiState {
    private final boolean loading;
    private final ComicDetailEntity comic;
    private final List<ChapterItem> chapters;
    private final String error;

    private ComicDetailUiState(boolean loading, ComicDetailEntity comic, List<ChapterItem> chapters, String error) {
        this.loading = loading;
        this.comic = comic;
        this.chapters = chapters;
        this.error = error;
    }

    public static ComicDetailUiState loading() { return new ComicDetailUiState(true, null, null, null); }
    public static ComicDetailUiState success(ComicDetailEntity comic, List<ChapterItem> chapters) { return new ComicDetailUiState(false, comic, chapters, null); }
    public static ComicDetailUiState error(String msg) { return new ComicDetailUiState(false, null, null, msg); }

    public boolean isLoading() { return loading; }
    public ComicDetailEntity getComic() { return comic; }
    public List<ChapterItem> getChapters() { return chapters; }
    public String getError() { return error; }
}
