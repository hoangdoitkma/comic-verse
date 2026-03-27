package com.example.comicversev1.di;

import android.content.Context;

import androidx.room.Room;

import com.example.comicversev1.data.local.AppDatabase;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.dao.ComicCacheDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "comicverse.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    ReadingHistoryDao provideReadingHistoryDao(AppDatabase db) {
        return db.readingHistoryDao();
    }

    @Provides
    @Singleton
    ComicCacheDao provideComicCacheDao(AppDatabase db) {
        return db.comicCacheDao();
    }

}
