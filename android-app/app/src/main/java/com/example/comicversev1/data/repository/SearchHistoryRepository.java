package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.HotSearchDTO;
import com.example.comicversev1.data.model.SearchHistoryItemDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface SearchHistoryRepository {
    Completable recordSearch(String keyword, String type);

    Single<List<SearchHistoryItemDTO>> getSearchHistory(String type, int limit);

    Single<List<HotSearchDTO>> getHotSearches(String type, int limit);

    Completable deleteSearchKeyword(String keyword, String type);

    Completable clearSearchHistory(String type);
}
