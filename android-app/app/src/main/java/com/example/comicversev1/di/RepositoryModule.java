package com.example.comicversev1.di;

import com.example.comicversev1.data.repository.AuthRepository;
import com.example.comicversev1.data.repository.AuthRepositoryImpl;
import com.example.comicversev1.data.repository.CommentRepository;
import com.example.comicversev1.data.repository.CommentRepositoryImpl;
import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.data.repository.ComicRepositoryImpl;
import com.example.comicversev1.data.repository.HomeRepository;
import com.example.comicversev1.data.repository.HomeRepositoryImpl;
import com.example.comicversev1.data.repository.NotificationRepository;
import com.example.comicversev1.data.repository.NotificationRepositoryImpl;
import com.example.comicversev1.data.repository.ReaderActionRepository;
import com.example.comicversev1.data.repository.ReaderActionRepositoryImpl;
import com.example.comicversev1.data.repository.ReadingHistoryRepository;
import com.example.comicversev1.data.repository.ReadingHistoryRepositoryImpl;
import com.example.comicversev1.data.repository.SearchHistoryRepository;
import com.example.comicversev1.data.repository.SearchHistoryRepositoryImpl;
import com.example.comicversev1.data.repository.UserDataRepository;
import com.example.comicversev1.data.repository.UserDataRepositoryImpl;
import com.example.comicversev1.data.repository.UserProfileRepository;
import com.example.comicversev1.data.repository.UserProfileRepositoryImpl;
import com.example.comicversev1.data.repository.VipRepository;
import com.example.comicversev1.data.repository.VipRepositoryImpl;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract AuthRepository bindAuthRepository(AuthRepositoryImpl impl);

    @Binds
    @Singleton
    abstract ComicRepository bindComicRepository(ComicRepositoryImpl impl);

    @Binds
    @Singleton
    abstract HomeRepository bindHomeRepository(HomeRepositoryImpl impl);

    @Binds
    @Singleton
    abstract NotificationRepository bindNotificationRepository(NotificationRepositoryImpl impl);

    @Binds
    @Singleton
    abstract ReadingHistoryRepository bindReadingHistoryRepository(ReadingHistoryRepositoryImpl impl);

    @Binds
    @Singleton
    abstract SearchHistoryRepository bindSearchHistoryRepository(SearchHistoryRepositoryImpl impl);

    @Binds
    @Singleton
    abstract CommentRepository bindCommentRepository(CommentRepositoryImpl impl);

    @Binds
    @Singleton
    abstract VipRepository bindVipRepository(VipRepositoryImpl impl);

    @Binds
    @Singleton
    abstract UserProfileRepository bindUserProfileRepository(UserProfileRepositoryImpl impl);

    @Binds
    @Singleton
    abstract UserDataRepository bindUserDataRepository(UserDataRepositoryImpl impl);

    @Binds
    @Singleton
    abstract ReaderActionRepository bindReaderActionRepository(ReaderActionRepositoryImpl impl);
}
