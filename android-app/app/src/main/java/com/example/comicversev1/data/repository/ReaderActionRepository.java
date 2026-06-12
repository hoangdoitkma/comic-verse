package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.model.ChapterReportRequest;
import com.example.comicversev1.data.model.ChapterReportResponse;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ReaderActionRepository {
    Single<List<ChapterItem>> getChaptersByComicId(int comicId);

    Completable syncReadingHistory(ReadingHistoryEntity entity);

    Completable trackView(int comicId, int chapterId);

    Single<ChapterReportResponse> reportChapter(int chapterId, ChapterReportRequest request);
}
