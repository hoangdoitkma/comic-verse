package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.model.ReadingHistoryRequest;
import com.example.comicversev1.data.model.ReadingHistorySyncDTO;
import com.example.comicversev1.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class ReadingHistorySyncRepository {

    private static final String TAG = "HistorySyncRepo";

    private final ApiService apiService;
    private final ReadingHistoryDao readingHistoryDao;
    private final SharedPreferences prefs;

    @Inject
    public ReadingHistorySyncRepository(ApiService apiService,
                                        ReadingHistoryDao readingHistoryDao,
                                        SharedPreferences prefs) {
        this.apiService = apiService;
        this.readingHistoryDao = readingHistoryDao;
        this.prefs = prefs;
    }

    public Completable syncWithServer() {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        if (token == null || token.isEmpty()) {
            return Completable.complete();
        }

        return apiService.getReadingHistory()
                .subscribeOn(Schedulers.io())
                .map(response -> response != null && response.getData() != null
                        ? response.getData()
                        : new ArrayList<ReadingHistorySyncDTO>())
                .flatMapCompletable(serverItems -> mergeServerHistory(serverItems)
                        .andThen(uploadLocalHistory()))
                .doOnError(error -> Log.e(TAG, "Reading history sync failed", error));
    }

    private Completable mergeServerHistory(List<ReadingHistorySyncDTO> serverItems) {
        if (serverItems == null || serverItems.isEmpty()) {
            return Completable.complete();
        }

        return readingHistoryDao.getAllHistory()
                .flatMapCompletable(localItems -> {
                    Map<Integer, ReadingHistoryEntity> localByComicId = new HashMap<>();
                    for (ReadingHistoryEntity entity : localItems) {
                        localByComicId.put(entity.comicId, entity);
                    }

                    List<ReadingHistoryEntity> upserts = new ArrayList<>();
                    for (ReadingHistorySyncDTO dto : serverItems) {
                        if (dto == null || dto.comicId <= 0 || dto.chapterId <= 0) {
                            continue;
                        }

                        long serverReadAt = dto.updatedAtMillis > 0
                                ? dto.updatedAtMillis
                                : System.currentTimeMillis();
                        ReadingHistoryEntity local = localByComicId.get(dto.comicId);
                        if (local == null || serverReadAt > local.readAt) {
                            upserts.add(mapToEntity(dto, serverReadAt));
                        }
                    }

                    if (upserts.isEmpty()) {
                        return Completable.complete();
                    }
                    return readingHistoryDao.insertOrUpdateAll(upserts);
                });
    }

    private Completable uploadLocalHistory() {
        return readingHistoryDao.getAllHistory()
                .flatMapCompletable(historyList -> {
                    if (historyList == null || historyList.isEmpty()) {
                        return Completable.complete();
                    }

                    return Observable.fromIterable(historyList)
                            .filter(entity -> entity.comicId > 0 && entity.chapterId > 0)
                            .map(entity -> new ReadingHistoryRequest(
                                    entity.comicId,
                                    entity.chapterId,
                                    Math.max(entity.pageIndex, 0)))
                            .toList()
                            .flatMapCompletable(requests -> requests.isEmpty()
                                    ? Completable.complete()
                                    : apiService.syncReadingHistory(requests));
                });
    }

    private ReadingHistoryEntity mapToEntity(ReadingHistorySyncDTO dto, long readAt) {
        ReadingHistoryEntity entity = new ReadingHistoryEntity();
        entity.comicId = dto.comicId;
        entity.chapterId = dto.chapterId;
        entity.pageIndex = Math.max(dto.lastPage, 0);
        entity.readAt = readAt;
        entity.percent = 0;
        entity.comicTitle = dto.title != null ? dto.title : "";
        entity.chapterTitle = dto.chapterTitle != null ? dto.chapterTitle : "Chapter " + dto.chapterId;
        entity.coverUrl = dto.thumbnailUrl != null ? dto.thumbnailUrl : "";
        entity.slug = dto.slug != null ? dto.slug : "";
        entity.authorName = dto.authorName != null ? dto.authorName : "";
        entity.viewCount = dto.viewCount;
        entity.comicType = dto.contentType != null ? dto.contentType : "COMIC";
        return entity;
    }
}
