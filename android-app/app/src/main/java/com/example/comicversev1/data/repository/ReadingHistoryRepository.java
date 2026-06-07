package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface ReadingHistoryRepository {
    Flowable<List<HomeContent.ComicCard>> observeRecentCards(String contentType);

    Flowable<List<HomeContent.ComicCard>> observeAllCards(String contentType);

    Single<ReadingHistoryEntity> getHistoryForComic(int comicId);

    Completable deleteByComicId(int comicId);

    Completable clearAll();
}
