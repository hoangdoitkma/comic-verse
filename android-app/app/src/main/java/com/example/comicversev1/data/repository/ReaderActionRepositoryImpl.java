package com.example.comicversev1.data.repository;

import android.util.Log;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.model.ChapterItemDTO;
import com.example.comicversev1.data.model.ChapterReportRequest;
import com.example.comicversev1.data.model.ChapterReportResponse;
import com.example.comicversev1.data.model.ReadingHistoryRequest;
import com.example.comicversev1.data.model.ViewTrackingRequest;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class ReaderActionRepositoryImpl implements ReaderActionRepository {

    private static final String TAG = "ReaderActionRepo";

    private final ApiService apiService;

    @Inject
    public ReaderActionRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<List<ChapterItem>> getChaptersByComicId(int comicId) {
        return apiService.getChaptersById(comicId)
                .map(response -> {
                    List<ChapterItem> chapters = new ArrayList<>();
                    if (response != null && response.getData() != null) {
                        for (ChapterItemDTO dto : response.getData()) {
                            chapters.add(new ChapterItem(dto.getId(), dto.getTitle(), dto.getAccessType()));
                        }
                    }
                    return chapters;
                });
    }

    @Override
    public Completable syncReadingHistory(ReadingHistoryEntity entity) {
        return apiService.updateReadingHistory(new ReadingHistoryRequest(
                        entity.comicId,
                        entity.chapterId,
                        Math.max(entity.pageIndex, 0)))
                .onErrorComplete(error -> {
                    Log.e(TAG, "Server history sync failed: " + error.getMessage());
                    return true;
                });
    }

    @Override
    public Completable trackView(int comicId, int chapterId) {
        return apiService.trackView(new ViewTrackingRequest(comicId, chapterId));
    }

    @Override
    public Single<ChapterReportResponse> reportChapter(int chapterId, ChapterReportRequest request) {
        return apiService.reportChapter(chapterId, request)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    throw new Exception(response.getMessage() != null ? response.getMessage() : "Khong the gui bao cao");
                });
    }
}
