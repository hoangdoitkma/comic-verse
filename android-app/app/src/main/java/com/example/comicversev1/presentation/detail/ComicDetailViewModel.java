package com.example.comicversev1.presentation.detail;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.domain.entity.ChapterItem;
import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.domain.usecase.GetChaptersUseCase;
import com.example.comicversev1.domain.usecase.GetComicDetailUseCase;

import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class ComicDetailViewModel extends ViewModel {

    private final GetComicDetailUseCase getComicDetailUseCase;
    private final GetChaptersUseCase getChaptersUseCase;
    private final ReadingHistoryDao readingHistoryDao;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final String slug;

    private final MutableLiveData<ComicDetailUiState> _uiState = new MutableLiveData<>(ComicDetailUiState.loading());
    public LiveData<ComicDetailUiState> uiState() { return _uiState; }

    // Saved reading progress for this comic (chapterId, or -1 if none)
    private final MutableLiveData<ReadingHistoryEntity> _savedProgress = new MutableLiveData<>(null);
    public LiveData<ReadingHistoryEntity> savedProgress() { return _savedProgress; }

    @Inject
    public ComicDetailViewModel(GetComicDetailUseCase getComicDetailUseCase,
                                GetChaptersUseCase getChaptersUseCase,
                                ReadingHistoryDao readingHistoryDao,
                                SavedStateHandle savedStateHandle) {
        this.getComicDetailUseCase = getComicDetailUseCase;
        this.getChaptersUseCase = getChaptersUseCase;
        this.readingHistoryDao = readingHistoryDao;
        this.slug = savedStateHandle.get("slug");
        loadData();
    }

    private void loadData() {
        disposables.add(
                getComicDetailUseCase.execute(slug)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onComicLoaded, this::onError)
        );
    }

    private void onComicLoaded(ComicDetailEntity comic) {
        // Load chapters
        disposables.add(
                getChaptersUseCase.execute(slug)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chapters -> {
                            _uiState.setValue(ComicDetailUiState.success(comic, chapters));
                            // Load saved reading progress for this comic
                            loadSavedProgress(comic.getId());
                        }, throwable -> _uiState.setValue(ComicDetailUiState.success(comic, Collections.emptyList())))
        );
    }

    /**
     * Load saved reading history for this comic from Room DB
     */
    private void loadSavedProgress(int comicId) {
        disposables.add(
                readingHistoryDao.getHistoryForComic(comicId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                history -> {
                                    Log.d("ComicDetailVM", "Found saved progress: chapter=" + history.chapterId);
                                    _savedProgress.setValue(history);
                                },
                                throwable -> {
                                    // No saved history, ignore
                                    Log.d("ComicDetailVM", "No saved progress for comic " + comicId);
                                }
                        )
        );
    }

    private void onError(Throwable throwable) {
        _uiState.setValue(ComicDetailUiState.error(throwable.getMessage()));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
