package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.domain.entity.ComicEntity;
import com.example.comicversev1.data.repository.ComicRepository;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetComicsUseCase {
    private final ComicRepository repository;

    @Inject
    public GetComicsUseCase(ComicRepository repository) {
        this.repository = repository;
    }

    public Single<List<ComicEntity>> execute(int page, int limit) {
        return repository.getComics(page, limit);
    }

    public Single<List<ComicEntity>> execute(int page, int limit, String keyword, String type) {
        return repository.getComics(page, limit, keyword, type);
    }

    public Single<List<ComicEntity>> execute(int page, int limit, String keyword, String type, String country, Integer genreId, String status) {
        return repository.getComics(page, limit, keyword, type, country, genreId, status);
    }
}

