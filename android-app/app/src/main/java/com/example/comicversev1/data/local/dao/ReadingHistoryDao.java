package com.example.comicversev1.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ReadingHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrUpdate(ReadingHistoryEntity entity);

    @Query("SELECT * FROM reading_history WHERE comic_id = :comicId ORDER BY read_at DESC LIMIT 1")
    Single<ReadingHistoryEntity> getHistoryForComic(int comicId);

    @Query("SELECT * FROM reading_history ORDER BY read_at DESC LIMIT 20")
    Flowable<List<ReadingHistoryEntity>> getRecentHistory();

    @Query("DELETE FROM reading_history WHERE comic_id = :comicId")
    Completable deleteHistoryByComicId(int comicId);
}

