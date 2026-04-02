package com.example.comicversev1.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.comicversev1.data.local.entity.ComicCacheEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface ComicCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable upsertAll(List<ComicCacheEntity> comics);

    @Query("DELETE FROM comic_cache")
    Completable clear();

    @Query("SELECT * FROM comic_cache ORDER BY updated_at DESC")
    Flowable<List<ComicCacheEntity>> observeAll();

    @Query("DELETE FROM comic_cache WHERE slug = :slug")
    Completable deleteBySlug(String slug);
}

