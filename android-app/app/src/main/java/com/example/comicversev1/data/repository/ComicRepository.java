package com.example.comicversev1.data.repository;

import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.domain.entity.ComicEntity;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.data.model.GenreDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface ComicRepository {
    Single<List<ComicEntity>> getComics(int page, int limit);
    Single<List<ComicEntity>> getComics(int page, int limit, String keyword, String type);
    Single<List<ComicEntity>> getComics(int page, int limit, String keyword, String type, String country, Integer genreId, String status);
    Single<ComicDetailEntity> getComicDetail(String slug);
    Single<ChapterEntity> getChapterDetail(int chapterId);
    Single<List<ChapterItem>> getChapters(String slug);
    Single<List<GenreDTO>> getGenres();
    Flowable<List<ComicEntity>> observeCachedComics();
    Single<com.example.comicversev1.data.model.ChapterReportResponse> reportChapter(int chapterId, com.example.comicversev1.data.model.ChapterReportRequest request);
}
