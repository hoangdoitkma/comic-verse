package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.data.repository.ComicRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetChapterDetailUseCase {
    private final ComicRepository repository;

    @Inject
    public GetChapterDetailUseCase(ComicRepository repository) {
        this.repository = repository;
    }

    public Single<ChapterEntity> execute(int chapterId) {
        return repository.getChapterDetail(chapterId);
    }
}

