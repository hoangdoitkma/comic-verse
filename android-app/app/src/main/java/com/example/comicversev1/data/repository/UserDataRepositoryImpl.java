package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.model.FavoriteRequest;
import com.example.comicversev1.data.model.ReadingHistoryRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Singleton
public class UserDataRepositoryImpl implements UserDataRepository {

    private final ApiService apiService;
    private final ReadingHistoryDao readingHistoryDao;
    private final FavoriteComicDao favoriteComicDao;

    @Inject
    public UserDataRepositoryImpl(ApiService apiService,
                                  ReadingHistoryDao readingHistoryDao,
                                  FavoriteComicDao favoriteComicDao) {
        this.apiService = apiService;
        this.readingHistoryDao = readingHistoryDao;
        this.favoriteComicDao = favoriteComicDao;
    }

    @Override
    public Completable syncLocalDataToServer() {
        return syncReadingHistory()
                .andThen(syncFavorites());
    }

    @Override
    public Completable clearLocalUserData() {
        return readingHistoryDao.deleteAllHistory()
                .onErrorComplete()
                .andThen(favoriteComicDao.deleteAllFavorites().onErrorComplete());
    }

    private Completable syncReadingHistory() {
        return readingHistoryDao.getAllHistory()
                .flatMapCompletable(historyList -> {
                    if (historyList == null || historyList.isEmpty()) {
                        return Completable.complete();
                    }
                    return Observable.fromIterable(historyList)
                            .map(entity -> new ReadingHistoryRequest(
                                    entity.comicId,
                                    entity.chapterId,
                                    entity.pageIndex))
                            .toList()
                            .flatMapCompletable(apiService::syncReadingHistory);
                });
    }

    private Completable syncFavorites() {
        return favoriteComicDao.getAllFavorites()
                .flatMapCompletable(favorites -> {
                    if (favorites == null || favorites.isEmpty()) {
                        return Completable.complete();
                    }
                    return Observable.fromIterable(favorites)
                            .filter(entity -> entity.slug != null && !entity.slug.trim().isEmpty())
                            .map(entity -> new FavoriteRequest(entity.slug))
                            .toList()
                            .flatMapCompletable(requests -> requests.isEmpty()
                                    ? Completable.complete()
                                    : apiService.syncFavorites(requests));
                });
    }
}
