package com.example.comicversev1.presentation.discover;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.model.GenreDTO;
import com.example.comicversev1.data.model.HotSearchDTO;
import com.example.comicversev1.data.model.SearchHistoryItemDTO;
import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.data.repository.SearchHistoryRepository;
import com.example.comicversev1.domain.entity.ComicEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class DiscoverViewModel extends ViewModel {
    private static final String TAG = "DiscoverVM";
    private static final int SEARCH_LIMIT = 20;

    private final MutableLiveData<SearchParams> searchParams = new MutableLiveData<>();
    private final MutableLiveData<List<ComicEntity>> comicResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ComicEntity>> novelResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final MutableLiveData<List<SearchHistoryItemDTO>> searchHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HotSearchDTO>> hotSearches = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GenreDTO>> genres = new MutableLiveData<>(new ArrayList<>());
    private final ComicRepository comicRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public DiscoverViewModel(ComicRepository repository,
                             SearchHistoryRepository searchHistoryRepository) {
        this.comicRepository = repository;
        this.searchHistoryRepository = searchHistoryRepository;
        SearchParams initial = new SearchParams(null, null, null, null);
        searchParams.setValue(initial);
        loadResults(initial);
    }

    public LiveData<List<ComicEntity>> comicResults() {
        return comicResults;
    }

    public LiveData<List<ComicEntity>> novelResults() {
        return novelResults;
    }

    public LiveData<Boolean> refreshing() {
        return refreshing;
    }

    public LiveData<List<SearchHistoryItemDTO>> searchHistory() {
        return searchHistory;
    }

    public LiveData<List<HotSearchDTO>> hotSearches() {
        return hotSearches;
    }

    public LiveData<List<GenreDTO>> genres() {
        return genres;
    }

    public void search(String keyword, String ignoredType) {
        SearchParams current = searchParams.getValue();
        setSearchParams(normalizeNullable(keyword),
                current != null ? current.country : null,
                current != null ? current.genreId : null,
                current != null ? current.status : null);
    }

    public void updateFilters(String country, Integer genreId, String status) {
        SearchParams current = searchParams.getValue();
        setSearchParams(current != null ? current.keyword : null,
                normalizeNullable(country),
                genreId,
                normalizeNullable(status));
    }

    public void refreshResults() {
        SearchParams current = searchParams.getValue();
        loadResults(current != null ? current : new SearchParams(null, null, null, null));
    }

    private void setSearchParams(String keyword, String country, Integer genreId, String status) {
        SearchParams current = searchParams.getValue();
        if (current != null && current.equalsTo(keyword, country, genreId, status)) {
            return;
        }
        SearchParams next = new SearchParams(keyword, country, genreId, status);
        searchParams.setValue(next);
        loadResults(next);
    }

    private void loadResults(SearchParams params) {
        refreshing.setValue(true);
        disposables.add(Single.zip(
                        comicRepository.getComics(0, SEARCH_LIMIT, params.keyword, "COMIC", params.country, params.genreId, params.status)
                                .onErrorReturnItem(new ArrayList<>()),
                        comicRepository.getComics(0, SEARCH_LIMIT, params.keyword, "NOVEL", params.country, params.genreId, params.status)
                                .onErrorReturnItem(new ArrayList<>()),
                        SearchResults::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    comicResults.setValue(results.comics);
                    novelResults.setValue(results.novels);
                    refreshing.setValue(false);
                }, throwable -> {
                    Log.e(TAG, "Failed to load search results", throwable);
                    comicResults.setValue(new ArrayList<>());
                    novelResults.setValue(new ArrayList<>());
                    refreshing.setValue(false);
                }));
    }

    public void submitSearch(String keyword, String ignoredType) {
        String normalizedKeyword = normalizeNullable(keyword);
        search(normalizedKeyword, null);
        if (normalizedKeyword == null || normalizedKeyword.length() < 2) {
            return;
        }

        disposables.add(searchHistoryRepository.recordSearch(normalizedKeyword, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> loadSearchSuggestions(null),
                        throwable -> Log.e(TAG, "Failed to record search", throwable)
                ));
    }

    public void loadSearchSuggestions(String ignoredType) {
        disposables.add(searchHistoryRepository.getSearchHistory(null, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchHistory::setValue,
                        throwable -> {
                            Log.e(TAG, "Failed to load search history", throwable);
                            searchHistory.setValue(new ArrayList<>());
                        }));

        disposables.add(searchHistoryRepository.getHotSearches(null, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hotSearches::setValue,
                        throwable -> {
                            Log.e(TAG, "Failed to load hot searches", throwable);
                            hotSearches.setValue(new ArrayList<>());
                        }));
    }

    public void loadGenres() {
        disposables.add(comicRepository.getGenres()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(genres::setValue,
                        throwable -> {
                            Log.e(TAG, "Failed to load genres", throwable);
                            genres.setValue(new ArrayList<>());
                        }));
    }

    public void deleteSearchKeyword(String keyword, String ignoredType) {
        disposables.add(searchHistoryRepository.deleteSearchKeyword(keyword, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> loadSearchSuggestions(null),
                        throwable -> Log.e(TAG, "Failed to delete search keyword", throwable)
                ));
    }

    public void clearSearchHistory(String ignoredType) {
        disposables.add(searchHistoryRepository.clearSearchHistory(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> loadSearchSuggestions(null),
                        throwable -> Log.e(TAG, "Failed to clear search history", throwable)
                ));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }

    private static class SearchParams {
        final String keyword;
        final String country;
        final Integer genreId;
        final String status;

        SearchParams(String keyword, String country, Integer genreId, String status) {
            this.keyword = keyword;
            this.country = country;
            this.genreId = genreId;
            this.status = status;
        }

        boolean equalsTo(String keyword, String country, Integer genreId, String status) {
            return java.util.Objects.equals(this.keyword, keyword)
                    && java.util.Objects.equals(this.country, country)
                    && java.util.Objects.equals(this.genreId, genreId)
                    && java.util.Objects.equals(this.status, status);
        }
    }

    private static class SearchResults {
        final List<ComicEntity> comics;
        final List<ComicEntity> novels;

        SearchResults(List<ComicEntity> comics, List<ComicEntity> novels) {
            this.comics = comics;
            this.novels = novels;
        }
    }
}
