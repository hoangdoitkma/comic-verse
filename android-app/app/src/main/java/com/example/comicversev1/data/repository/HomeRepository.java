package com.example.comicversev1.data.repository;

import com.example.comicversev1.domain.entity.HomeContent;

import io.reactivex.rxjava3.core.Single;

public interface HomeRepository {
    Single<HomeContent> loadHomeContent();
}

