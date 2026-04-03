package com.example.comicversev1.presentation.detail;

import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.data.local.entity.FavoriteComicEntity;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.ComicCacheDao;
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
    private final ComicCacheDao comicCacheDao;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final String slug;
    private final int comicId;

    private final MutableLiveData<ComicDetailUiState> _uiState = new MutableLiveData<>(ComicDetailUiState.loading());
    public LiveData<ComicDetailUiState> uiState() { return _uiState; }

    // Saved reading progress for this comic (chapterId, or -1 if none)
    private final MutableLiveData<ReadingHistoryEntity> _savedProgress = new MutableLiveData<>(null);
    public LiveData<ReadingHistoryEntity> savedProgress() { return _savedProgress; }

    private final MutableLiveData<Boolean> _isFavorite = new MutableLiveData<>(false);
    public LiveData<Boolean> isFavorite() { return _isFavorite; }

    private final FavoriteComicDao favoriteComicDao;

    @Inject
    public ComicDetailViewModel(GetComicDetailUseCase getComicDetailUseCase,
                                GetChaptersUseCase getChaptersUseCase,
                                ReadingHistoryDao readingHistoryDao,
                                ComicCacheDao comicCacheDao,
                                FavoriteComicDao favoriteComicDao,
                                SavedStateHandle savedStateHandle) {
        this.getComicDetailUseCase = getComicDetailUseCase;
        this.getChaptersUseCase = getChaptersUseCase;
        this.readingHistoryDao = readingHistoryDao;
        this.comicCacheDao = comicCacheDao;
        this.favoriteComicDao = favoriteComicDao;
        this.slug = savedStateHandle.get("slug");
        
        Integer cId = savedStateHandle.get("comicId");
        this.comicId = cId != null ? cId : 0;
        
        loadData();
        checkFavoriteStatus();
    }


    private void checkFavoriteStatus() {
        disposables.add(
                favoriteComicDao.checkIsFavorite(slug)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isFav -> _isFavorite.setValue(isFav),
                                throwable -> Log.e("ComicDetailVM", "Lỗi check favorite", throwable))
        );
    }

    public void toggleFavorite(String title, String coverUrl, String type) {
        Boolean current = _isFavorite.getValue();
        if (current != null && current) {
            // Remove
            disposables.add(
                    favoriteComicDao.deleteFavoriteBySlug(slug)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> _isFavorite.setValue(false),
                                    throwable -> Log.e("ComicDetailVM", "Lỗi xóa favorite", throwable))
            );
        } else {
            // Add
            FavoriteComicEntity entity = new FavoriteComicEntity(slug, title, coverUrl, type, System.currentTimeMillis());
            disposables.add(
                    favoriteComicDao.insertOrUpdate(entity)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> _isFavorite.setValue(true),
                                    throwable -> Log.e("ComicDetailVM", "Lỗi thêm favorite", throwable))
            );
        }
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
    public void loadSavedProgress(int comicId) {
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
        boolean is404 = false;
        if (throwable instanceof com.example.comicversev1.data.model.NetworkException) {
            if (((com.example.comicversev1.data.model.NetworkException) throwable).getErrorCode() == 404) is404 = true;
        } else if (throwable instanceof retrofit2.HttpException) {
            if (((retrofit2.HttpException) throwable).code() == 404) is404 = true;
        }
        
        if (is404) {
            Log.d("ComicDetailVM", "Comic not found. Deleting stale local data.");
            disposables.add(comicCacheDao.deleteBySlug(slug).subscribeOn(Schedulers.io()).subscribe());
            if (comicId > 0) {
                disposables.add(readingHistoryDao.deleteHistoryByComicId(comicId).subscribeOn(Schedulers.io()).subscribe());
            }
            _uiState.setValue(ComicDetailUiState.error("COMIC_DELETED"));
            return;
        }
        
        _uiState.setValue(ComicDetailUiState.error(throwable.getMessage()));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
