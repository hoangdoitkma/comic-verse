package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.HotSearchDTO;
import com.example.comicversev1.data.model.SearchHistoryItemDTO;
import com.example.comicversev1.data.model.SearchHistoryRequest;
import com.example.comicversev1.utils.Constants;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class SearchHistoryRepositoryImpl implements SearchHistoryRepository {

    private final ApiService apiService;
    private final SharedPreferences prefs;

    @Inject
    public SearchHistoryRepositoryImpl(ApiService apiService, SharedPreferences prefs) {
        this.apiService = apiService;
        this.prefs = prefs;
    }

    @Override
    public Completable recordSearch(String keyword, String type) {
        String normalizedKeyword = sanitize(keyword);
        if (normalizedKeyword.length() < 2) {
            return Completable.complete();
        }
        return apiService.recordSearch(new SearchHistoryRequest(normalizedKeyword, sanitizeType(type)));
    }

    @Override
    public Single<List<SearchHistoryItemDTO>> getSearchHistory(String type, int limit) {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        if (token == null || token.isEmpty()) {
            return Single.just(Collections.emptyList());
        }
        return apiService.getSearchHistory(sanitizeType(type), limit)
                .map(response -> response.isSuccess() && response.getData() != null
                        ? response.getData()
                        : Collections.<SearchHistoryItemDTO>emptyList())
                .onErrorReturnItem(Collections.emptyList());
    }

    @Override
    public Single<List<HotSearchDTO>> getHotSearches(String type, int limit) {
        return apiService.getHotSearches(sanitizeType(type), limit)
                .map(response -> response.isSuccess() && response.getData() != null
                        ? response.getData()
                        : Collections.<HotSearchDTO>emptyList())
                .onErrorReturnItem(Collections.emptyList());
    }

    @Override
    public Completable deleteSearchKeyword(String keyword, String type) {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        String normalizedKeyword = sanitize(keyword);
        if (token == null || token.isEmpty() || normalizedKeyword.isEmpty()) {
            return Completable.complete();
        }
        return apiService.deleteSearchHistoryItem(normalizedKeyword, sanitizeType(type))
                .onErrorComplete();
    }

    @Override
    public Completable clearSearchHistory(String type) {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");
        if (token == null || token.isEmpty()) {
            return Completable.complete();
        }
        return apiService.clearSearchHistory(sanitizeType(type))
                .onErrorComplete();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private String sanitizeType(String type) {
        String sanitized = sanitize(type);
        return sanitized.isEmpty() ? null : sanitized;
    }
}
