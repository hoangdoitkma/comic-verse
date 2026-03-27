package com.example.comicversev1.di;

import com.example.comicversev1.data.repository.AuthRepository;
import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.domain.usecase.GetChaptersUseCase;
import com.example.comicversev1.domain.usecase.GetChapterDetailUseCase;
import com.example.comicversev1.domain.usecase.GetComicDetailUseCase;
import com.example.comicversev1.domain.usecase.GetComicsUseCase;
import com.example.comicversev1.domain.usecase.LoginUseCase;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    LoginUseCase provideLoginUseCase(AuthRepository repository) {
        return new LoginUseCase(repository);
    }

    @Provides
    GetComicsUseCase provideGetComicsUseCase(ComicRepository repository) {
        return new GetComicsUseCase(repository);
    }

    @Provides
    GetComicDetailUseCase provideGetComicDetailUseCase(ComicRepository repository) {
        return new GetComicDetailUseCase(repository);
    }

    @Provides
    GetChaptersUseCase provideGetChaptersUseCase(ComicRepository repository) {
        return new GetChaptersUseCase(repository);
    }

    @Provides
    GetChapterDetailUseCase provideGetChapterDetailUseCase(ComicRepository repository) {
        return new GetChapterDetailUseCase(repository);
    }
}
