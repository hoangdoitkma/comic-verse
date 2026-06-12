package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;
import android.util.Log;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.data.local.entity.FavoriteComicEntity;
import com.example.comicversev1.data.model.FavoriteDTO;
import com.example.comicversev1.data.model.FavoriteRequest;
import com.example.comicversev1.domain.entity.HomeContent;
import com.example.comicversev1.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Singleton
public class FavoriteSyncRepository {

    private static final String TAG = "FavoriteSyncRepo";

    private final ApiService apiService;
    private final FavoriteComicDao favoriteComicDao;
    private final SharedPreferences prefs;

    @Inject
    public FavoriteSyncRepository(ApiService apiService,
                                  FavoriteComicDao favoriteComicDao,
                                  SharedPreferences prefs) {
        this.apiService = apiService;
        this.favoriteComicDao = favoriteComicDao;
        this.prefs = prefs;
    }

    public Completable syncWithServer() {
        if (!isLoggedIn()) {
            return Completable.complete();
        }

        return apiService.getFavorites()
                .subscribeOn(Schedulers.io())
                .map(response -> response != null && response.getData() != null
                        ? response.getData()
                        : new ArrayList<FavoriteDTO>())
                .flatMapCompletable(serverItems -> mergeServerFavorites(serverItems)
                        .andThen(uploadLocalFavorites()))
                .doOnError(error -> Log.e(TAG, "Favorite sync failed", error));
    }

    public Completable addFavorite(FavoriteComicEntity entity) {
        Completable localSave = favoriteComicDao.insertOrUpdate(entity);
        if (!isLoggedIn()) {
            return localSave;
        }
        return localSave.andThen(apiService.addFavorite(new FavoriteRequest(entity.slug))
                .onErrorComplete(error -> {
                    Log.e(TAG, "Add favorite server sync failed: " + error.getMessage());
                    return true;
                }));
    }

    public Completable removeFavorite(String slug) {
        Completable localDelete = favoriteComicDao.deleteFavoriteBySlug(slug);
        if (!isLoggedIn()) {
            return localDelete;
        }
        return localDelete.andThen(apiService.removeFavorite(slug)
                .onErrorComplete(error -> {
                    Log.e(TAG, "Remove favorite server sync failed: " + error.getMessage());
                    return true;
                }));
    }

    public Single<Boolean> isFavorite(String slug) {
        return favoriteComicDao.checkIsFavorite(slug);
    }

    public Flowable<List<HomeContent.ComicCard>> observeFavoriteCards(String contentType) {
        return favoriteComicDao.getAllFavoritesByType(contentType)
                .map(entities -> {
                    List<HomeContent.ComicCard> cards = new ArrayList<>();
                    if (entities == null) {
                        return cards;
                    }
                    for (FavoriteComicEntity entity : entities) {
                        cards.add(new HomeContent.ComicCard(
                                entity.slug,
                                entity.comicTitle,
                                "",
                                entity.coverUrl,
                                0,
                                0,
                                0,
                                "",
                                "FREE",
                                ""
                        ));
                    }
                    return cards;
                });
    }

    private Completable mergeServerFavorites(List<FavoriteDTO> serverItems) {
        if (serverItems == null || serverItems.isEmpty()) {
            return Completable.complete();
        }

        List<FavoriteComicEntity> upserts = new ArrayList<>();
        for (FavoriteDTO dto : serverItems) {
            if (dto == null || dto.slug == null || dto.slug.trim().isEmpty()) {
                continue;
            }
            upserts.add(mapToEntity(dto));
        }

        if (upserts.isEmpty()) {
            return Completable.complete();
        }
        return favoriteComicDao.insertOrUpdateAll(upserts);
    }

    private Completable uploadLocalFavorites() {
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

    private FavoriteComicEntity mapToEntity(FavoriteDTO dto) {
        return new FavoriteComicEntity(
                dto.slug,
                dto.title != null ? dto.title : "",
                dto.thumbnailUrl != null ? dto.thumbnailUrl : "",
                dto.contentType != null ? dto.contentType : "COMIC",
                dto.addedAtMillis > 0 ? dto.addedAtMillis : System.currentTimeMillis()
        );
    }

    private boolean isLoggedIn() {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        return token != null && !token.isEmpty();
    }
}
