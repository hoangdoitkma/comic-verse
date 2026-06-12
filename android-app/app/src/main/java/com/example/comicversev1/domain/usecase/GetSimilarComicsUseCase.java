package com.example.comicversev1.domain.usecase;

import com.example.comicversev1.data.repository.HomeRepository;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class GetSimilarComicsUseCase {
    private final HomeRepository homeRepository;

    @Inject
    public GetSimilarComicsUseCase(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public Single<List<HomeContent.ComicCard>> execute(String slug) {
        return homeRepository.getSimilarComics(slug);
    }
}
