package com.example.comicversev1.presentation.novel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.repository.FavoriteSyncRepository;
import com.example.comicversev1.data.repository.HomeRepository;
import com.example.comicversev1.data.repository.ReadingHistoryRepository;
import com.example.comicversev1.data.repository.ReadingHistorySyncRepository;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class NovelViewModel extends ViewModel {

    private static final String TAG = "NovelViewModel";

    private final MutableLiveData<NovelUiState> uiState = new MutableLiveData<>(NovelUiState.loading());
    private final MutableLiveData<List<HomeContent.ComicCard>> recentlyReadCards = new MutableLiveData<>(new ArrayList<>());
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final HomeRepository repository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ReadingHistorySyncRepository historySyncRepository;
    private final FavoriteSyncRepository favoriteSyncRepository;
    private boolean isReadingHistoryObserved = false;

    @Inject
    public NovelViewModel(HomeRepository repository,
                          ReadingHistoryRepository readingHistoryRepository,
                          ReadingHistorySyncRepository historySyncRepository,
                          FavoriteSyncRepository favoriteSyncRepository) {
        this.repository = repository;
        this.readingHistoryRepository = readingHistoryRepository;
        this.historySyncRepository = historySyncRepository;
        this.favoriteSyncRepository = favoriteSyncRepository;

        loadNovelContent();
        loadReadingHistory();
        syncReadingHistory();
        syncFavorites();
    }

    public void refresh() {
        loadNovelContent();
        loadReadingHistory();
        syncReadingHistory();
        syncFavorites();
    }

    private void loadNovelContent() {
        disposables.add(repository.loadNovelContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        content -> uiState.setValue(NovelUiState.from(content)),
                        throwable -> {
                            NovelUiState current = uiState.getValue();
                            uiState.setValue(current != null && !current.isLoading()
                                    ? current.withError("Lỗi tải dữ liệu")
                                    : NovelUiState.error("Lỗi tải dữ liệu"));
                        }
                ));
    }

    private void loadReadingHistory() {
        if (isReadingHistoryObserved) {
            return;
        }
        isReadingHistoryObserved = true;

        disposables.add(readingHistoryRepository.observeRecentCards("NOVEL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        recentlyReadCards::setValue,
                        throwable -> Log.e(TAG, "Failed to load local reading history", throwable)
                ));
    }

    private void syncReadingHistory() {
        disposables.add(historySyncRepository.syncWithServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "Reading history sync completed"),
                        throwable -> Log.e(TAG, "Reading history sync skipped/failed: " + throwable.getMessage())
                ));
    }

    private void syncFavorites() {
        disposables.add(favoriteSyncRepository.syncWithServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "Favorite sync completed"),
                        throwable -> Log.e(TAG, "Favorite sync skipped/failed: " + throwable.getMessage())
                ));
    }

    public LiveData<NovelUiState> getUiState() {
        return uiState;
    }

    public LiveData<List<HomeContent.ComicCard>> recentlyReadCards() {
        return recentlyReadCards;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
