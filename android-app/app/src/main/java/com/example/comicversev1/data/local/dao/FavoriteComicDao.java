package com.example.comicversev1.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.comicversev1.data.local.entity.FavoriteComicEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FavoriteComicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdate(FavoriteComicEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdateAll(List<FavoriteComicEntity> entities);

    @Query("DELETE FROM favorite_comics WHERE slug = :slug")
    Completable deleteFavoriteBySlug(String slug);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_comics WHERE slug = :slug LIMIT 1)")
    Single<Boolean> checkIsFavorite(String slug);

    @Query("SELECT * FROM favorite_comics WHERE comic_type = :comicType ORDER BY added_at DESC")
    Flowable<List<FavoriteComicEntity>> getAllFavoritesByType(String comicType);

    @Query("SELECT * FROM favorite_comics")
    Single<List<FavoriteComicEntity>> getAllFavorites();

    @Query("DELETE FROM favorite_comics")
    Completable deleteAllFavorites();
}
