package com.example.comicversev1.presentation.history;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
public class HistoryViewModel extends ViewModel {

    private static final String TAG = "HistoryVM";

    private final ReadingHistoryRepository readingHistoryRepository;
    private final ReadingHistorySyncRepository historySyncRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<HomeContent.ComicCard>> comicHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HomeContent.ComicCard>> novelHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);

    @Inject
    public HistoryViewModel(ReadingHistoryRepository readingHistoryRepository,
                            ReadingHistorySyncRepository historySyncRepository) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.historySyncRepository = historySyncRepository;
        loadHistory();
    }

    public LiveData<List<HomeContent.ComicCard>> comicHistory() {
        return comicHistory;
    }

    public LiveData<List<HomeContent.ComicCard>> novelHistory() {
        return novelHistory;
    }

    public LiveData<Boolean> refreshing() {
        return refreshing;
    }

    public void refresh() {
        refreshing.setValue(true);
        disposables.add(historySyncRepository.syncWithServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d(TAG, "Reading history sync completed");
                            refreshing.setValue(false);
                        },
                        throwable -> {
                            Log.e(TAG, "Reading history sync skipped/failed", throwable);
                            refreshing.setValue(false);
                        }
                ));
    }

    private void loadHistory() {
        disposables.add(readingHistoryRepository.observeAllCards("COMIC")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comicHistory::setValue,
                        throwable -> Log.e(TAG, "Error loading comic history", throwable)));

        disposables.add(readingHistoryRepository.observeAllCards("NOVEL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(novelHistory::setValue,
                        throwable -> Log.e(TAG, "Error loading novel history", throwable)));
    }

    public void clearAllHistory() {
        disposables.add(readingHistoryRepository.clearAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Log.d(TAG, "All reading history cleared"),
                        throwable -> Log.e(TAG, "Failed to clear history", throwable)
                ));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
