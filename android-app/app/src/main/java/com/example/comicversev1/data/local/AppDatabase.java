package com.example.comicversev1.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.dao.ComicCacheDao;
import com.example.comicversev1.data.local.entity.ComicCacheEntity;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;

@Database(entities = {ReadingHistoryEntity.class, ComicCacheEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReadingHistoryDao readingHistoryDao();
    public abstract ComicCacheDao comicCacheDao();
}
