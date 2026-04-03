package com.example.comicversev1.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.dao.ComicCacheDao;
import com.example.comicversev1.data.local.entity.ComicCacheEntity;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.data.local.entity.FavoriteComicEntity;

@Database(entities = {ReadingHistoryEntity.class, ComicCacheEntity.class, FavoriteComicEntity.class}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReadingHistoryDao readingHistoryDao();
    public abstract ComicCacheDao comicCacheDao();
    public abstract FavoriteComicDao favoriteComicDao();
}
