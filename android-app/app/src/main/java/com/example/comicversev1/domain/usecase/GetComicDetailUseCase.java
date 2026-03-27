package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.data.repository.ComicRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetComicDetailUseCase {
    private final ComicRepository repository;

    @Inject
    public GetComicDetailUseCase(ComicRepository repository) {
        this.repository = repository;
    }

    public Single<ComicDetailEntity> execute(String slug) {
        return repository.getComicDetail(slug);
    }
}
