package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetChaptersUseCase {
    private final ComicRepository repository;

    @Inject
    public GetChaptersUseCase(ComicRepository repository) {
        this.repository = repository;
    }

    public Single<List<ChapterItem>> execute(String slug) {
        return repository.getChapters(slug);
    }
}

