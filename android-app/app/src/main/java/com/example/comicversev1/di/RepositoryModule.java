package com.example.comicversev1.di;

import com.example.comicversev1.data.repository.AuthRepository;
import com.example.comicversev1.data.repository.AuthRepositoryImpl;
import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.data.repository.ComicRepositoryImpl;
import com.example.comicversev1.data.repository.HomeRepository;
import com.example.comicversev1.data.repository.HomeRepositoryImpl;

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
}
