package com.example.comicversev1.presentation.detail;

import com.example.comicversev1.data.local.entity.FavoriteComicEntity;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.ComicCacheDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.repository.FavoriteSyncRepository;
import com.example.comicversev1.data.repository.ReadingHistoryRepository;
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
    private final com.example.comicversev1.domain.usecase.GetSimilarComicsUseCase getSimilarComicsUseCase;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ComicCacheDao comicCacheDao;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final String slug;
    private final int comicId;

    private final MutableLiveData<ComicDetailUiState> _uiState = new MutableLiveData<>(ComicDetailUiState.loading());
    public LiveData<ComicDetailUiState> uiState() { return _uiState; }

    private final MutableLiveData<Boolean> _refreshing = new MutableLiveData<>(false);
    public LiveData<Boolean> refreshing() { return _refreshing; }

    // Saved reading progress for this comic (chapterId, or -1 if none)
    private final MutableLiveData<ReadingHistoryEntity> _savedProgress = new MutableLiveData<>(null);
    public LiveData<ReadingHistoryEntity> savedProgress() { return _savedProgress; }

    private final MutableLiveData<Boolean> _isFavorite = new MutableLiveData<>(false);
    public LiveData<Boolean> isFavorite() { return _isFavorite; }

    private final FavoriteSyncRepository favoriteSyncRepository;

    @Inject
    public ComicDetailViewModel(GetComicDetailUseCase getComicDetailUseCase,
                                GetChaptersUseCase getChaptersUseCase,
                                com.example.comicversev1.domain.usecase.GetSimilarComicsUseCase getSimilarComicsUseCase,
                                ReadingHistoryRepository readingHistoryRepository,
                                ComicCacheDao comicCacheDao,
                                FavoriteSyncRepository favoriteSyncRepository,
                                SavedStateHandle savedStateHandle) {
        this.getComicDetailUseCase = getComicDetailUseCase;
        this.getChaptersUseCase = getChaptersUseCase;
        this.getSimilarComicsUseCase = getSimilarComicsUseCase;
        this.readingHistoryRepository = readingHistoryRepository;
        this.comicCacheDao = comicCacheDao;
        this.favoriteSyncRepository = favoriteSyncRepository;
        this.slug = savedStateHandle.get("slug");
        
        Integer cId = savedStateHandle.get("comicId");
        this.comicId = cId != null ? cId : 0;
        
        loadData(false);
        checkFavoriteStatus();
    }


    private void checkFavoriteStatus() {
        disposables.add(
                favoriteSyncRepository.syncWithServer()
                        .onErrorComplete()
                        .andThen(favoriteSyncRepository.isFavorite(slug))
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
                    favoriteSyncRepository.removeFavorite(slug)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> _isFavorite.setValue(false),
                                    throwable -> Log.e("ComicDetailVM", "Lỗi xóa favorite", throwable))
            );
        } else {
            // Add
            FavoriteComicEntity entity = new FavoriteComicEntity(slug, title, coverUrl, type, System.currentTimeMillis());
            disposables.add(
                    favoriteSyncRepository.addFavorite(entity)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> _isFavorite.setValue(true),
                                    throwable -> Log.e("ComicDetailVM", "Lỗi thêm favorite", throwable))
            );
        }
    }

    public void refresh() {
        loadData(true);
        checkFavoriteStatus();
    }

    private void loadData(boolean trackRefresh) {
        if (trackRefresh) {
            _refreshing.setValue(true);
        }
        disposables.add(
                getComicDetailUseCase.execute(slug)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(comic -> onComicLoaded(comic, trackRefresh), throwable -> {
                            if (trackRefresh) {
                                _refreshing.setValue(false);
                            }
                            onError(throwable);
                        })
        );
    }

    private void onComicLoaded(ComicDetailEntity comic, boolean trackRefresh) {
        // Load chapters and similar comics in parallel
        io.reactivex.rxjava3.core.Single<java.util.List<ChapterItem>> chaptersSingle = getChaptersUseCase.execute(slug)
                .onErrorReturnItem(Collections.emptyList());
        
        io.reactivex.rxjava3.core.Single<java.util.List<com.example.comicversev1.domain.entity.HomeContent.ComicCard>> similarSingle = 
                getSimilarComicsUseCase.execute(slug)
                .onErrorReturnItem(Collections.emptyList());

        disposables.add(
                io.reactivex.rxjava3.core.Single.zip(chaptersSingle, similarSingle, (chapters, similarComics) -> 
                        ComicDetailUiState.success(comic, chapters, similarComics))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> {
                            _uiState.setValue(state);
                            if (trackRefresh) {
                                _refreshing.setValue(false);
                            }
                            // Load saved reading progress for this comic
                            loadSavedProgress(comic.getId());
                        }, throwable -> {
                            _uiState.setValue(ComicDetailUiState.success(comic, Collections.emptyList(), Collections.emptyList()));
                            if (trackRefresh) {
                                _refreshing.setValue(false);
                            }
                        })
        );
    }

    /**
     * Load saved reading history for this comic from Room DB
     */
    public void loadSavedProgress(int comicId) {
        disposables.add(
                readingHistoryRepository.getHistoryForComic(comicId)
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
                disposables.add(readingHistoryRepository.deleteByComicId(comicId).subscribeOn(Schedulers.io()).subscribe());
            }
            _uiState.setValue(ComicDetailUiState.error("COMIC_DELETED"));
            return;
        }
        
        _uiState.setValue(ComicDetailUiState.error(toUserMessage(throwable)));
    }

    private String toUserMessage(Throwable throwable) {
        if (throwable instanceof com.example.comicversev1.data.model.NetworkException) {
            String message = throwable.getMessage();
            return message != null && !message.trim().isEmpty()
                    ? message
                    : "Khong the tai du lieu truyen. Vui long thu lai.";
        }
        String message = throwable != null ? throwable.getMessage() : null;
        if (message == null || message.trim().isEmpty()) {
            return "Khong the tai du lieu truyen. Vui long thu lai.";
        }
        String lower = message.toLowerCase();
        if (message.length() > 140
                || message.contains("{")
                || lower.contains("payload")
                || lower.contains("jwt")
                || lower.contains("token")) {
            return "Phien dang nhap hoac du lieu tai lai khong hop le. Vui long thu lai.";
        }
        return message;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
