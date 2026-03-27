package com.example.comicversev1.presentation.reader;

import com.example.comicversev1.domain.entity.ChapterEntity;

public class ReaderUiState {
    private final boolean loading;
    private final boolean loadingMore;
    private final ChapterEntity chapter;
    private final String error;
    private final String currentChapterTitle;
    private final int currentPage;
    private final int totalPagesInChapter;

    private ReaderUiState(boolean loading, boolean loadingMore, ChapterEntity chapter,
                          String error, String currentChapterTitle, int currentPage, int totalPagesInChapter) {
        this.loading = loading;
        this.loadingMore = loadingMore;
        this.chapter = chapter;
        this.error = error;
        this.currentChapterTitle = currentChapterTitle;
        this.currentPage = currentPage;
        this.totalPagesInChapter = totalPagesInChapter;
    }

    public static ReaderUiState loading() {
        return new ReaderUiState(true, false, null, null, "", 0, 0);
    }

    public static ReaderUiState success(ChapterEntity chapter) {
        String title = chapter != null ? chapter.getTitle() : "";
        int total = chapter != null && chapter.getImages() != null ? chapter.getImages().size() : 0;
        return new ReaderUiState(false, false, chapter, null, title, 0, total);
    }

    public static ReaderUiState error(String message) {
        return new ReaderUiState(false, false, null, message, "", 0, 0);
    }

    public ReaderUiState withLoadingMore(boolean loadingMore) {
        return new ReaderUiState(this.loading, loadingMore, this.chapter, this.error,
                this.currentChapterTitle, this.currentPage, this.totalPagesInChapter);
    }

    public ReaderUiState withProgress(String chapterTitle, int currentPage, int totalPages) {
        return new ReaderUiState(this.loading, this.loadingMore, this.chapter, this.error,
                chapterTitle, currentPage, totalPages);
    }

    public boolean isLoading() { return loading; }
    public boolean isLoadingMore() { return loadingMore; }
    public ChapterEntity getChapter() { return chapter; }
    public String getError() { return error; }
    public String getCurrentChapterTitle() { return currentChapterTitle; }
    public int getCurrentPage() { return currentPage; }
    public int getTotalPagesInChapter() { return totalPagesInChapter; }
}
