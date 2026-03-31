package com.example.comicversev1.presentation.reader;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.domain.usecase.GetChapterDetailUseCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.ViewTrackingRequest;

import java.util.concurrent.TimeUnit;

@HiltViewModel
public class ReaderViewModel extends ViewModel {

    private static final String TAG = "ReaderViewModel";

    private final GetChapterDetailUseCase getChapterDetailUseCase;
    private final ReadingHistoryDao readingHistoryDao;
    private final ApiService apiService;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final int initialChapterId;
    private final int comicId;

    // Track loaded chapters to avoid duplicates
    private final Set<Integer> loadedChapterIds = new HashSet<>();
    // Ordered list of loaded chapter entities
    private final List<ChapterEntity> loadedChapters = new ArrayList<>();

    // The next chapter ID to load when user scrolls to bottom
    private Integer pendingNextChapterId = null;
    private boolean isLoadingMore = false;

    private final MutableLiveData<ReaderUiState> _uiState = new MutableLiveData<>(ReaderUiState.loading());
    public LiveData<ReaderUiState> uiState() { return _uiState; }

    // Event for appending new chapter pages
    private final MutableLiveData<ChapterEntity> _appendChapterEvent = new MutableLiveData<>();
    public LiveData<ChapterEntity> appendChapterEvent() { return _appendChapterEvent; }

    // Saved reading progress (loaded from Room DB)
    private int savedChapterId = -1;
    
    // One-shot scroll target. Fragment will consume this.
    private Integer pendingScrollToRelativePage = null;

    // RxJava subject to debounce save operations
    private final PublishSubject<ReadingHistoryEntity> saveProgressSubject = PublishSubject.create();

    @Inject
    public ReaderViewModel(GetChapterDetailUseCase getChapterDetailUseCase,
                           ReadingHistoryDao readingHistoryDao,
                           SavedStateHandle handle,
                           ApiService apiService) {
        this.getChapterDetailUseCase = getChapterDetailUseCase;
        this.readingHistoryDao = readingHistoryDao;
        this.apiService = apiService;
        this.initialChapterId = handle.get("chapterId");
        this.comicId = handle.get("comicId");

        setupSaveDebouncer();

        // First load saved progress, then load the chapter
        loadSavedProgressAndStart();
    }

    private void setupSaveDebouncer() {
        disposables.add(
                saveProgressSubject
                        .throttleLatest(1000, TimeUnit.MILLISECONDS) // Prevent rapid DB spam
                        .observeOn(Schedulers.io())
                        .flatMapCompletable(entity -> readingHistoryDao.insertOrUpdate(entity))
                        .subscribe(
                                () -> Log.d(TAG, ">>> DEBOUNCED SAVE OK"),
                                throwable -> Log.e(TAG, ">>> DEBOUNCED SAVE FAILED: " + throwable.getMessage())
                        )
        );
    }

    /**
     * Check Room DB for saved reading progress for this comic.
     * If found AND the saved chapter matches the initial chapter, we'll scroll to that page.
     * If saved chapter is different from initialChapterId, load the saved chapter instead.
     */
    private void loadSavedProgressAndStart() {
        disposables.add(
                readingHistoryDao.getHistoryForComic(comicId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(history -> {
                            // Phục hồi lịch sử
                            savedChapterId = history.chapterId;
                            Log.d(TAG, "Restored progress: chapter=" + savedChapterId + ", page=" + history.pageIndex);

                            // Nếu user chủ đích bấm thẳng vào cái chương đã lưu đó, ta mới khôi phục vị trí trang (scroll)
                            if (savedChapterId == initialChapterId && history.pageIndex > 0) {
                                pendingScrollToRelativePage = history.pageIndex;
                            }

                            // Luôn luôn load cái chương mà màn hình ComicDetailFragment yêu cầu Truyền sang!
                            // Không được phép tự ý nhảy sang savedChapterId (bởi vì ComicDetailFragment đã tự handle resume)
                            Log.d(TAG, "Starting from explicit chapter requested: " + initialChapterId);
                            loadChapter(initialChapterId, true);
                        }, throwable -> {
                            // No saved progress, start from the initial chapter
                            Log.d(TAG, "No saved progress, starting from explicit chapter " + initialChapterId);
                            loadChapter(initialChapterId, true);
                        })
        );
    }

    /**
     * Load a chapter by ID. If isInitial, it replaces the UI state.
     * Otherwise, it appends to the reader.
     */
    private void loadChapter(int chapterId, boolean isInitial) {
        if (loadedChapterIds.contains(chapterId)) return;

        if (!isInitial) {
            isLoadingMore = true;
            ReaderUiState current = _uiState.getValue();
            if (current != null) {
                _uiState.setValue(current.withLoadingMore(true));
            }
        }

        disposables.add(
                getChapterDetailUseCase.execute(chapterId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(chapter -> {
                            loadedChapterIds.add(chapter.getId());
                            loadedChapters.add(chapter);
                            pendingNextChapterId = chapter.getNextChapterId();
                            isLoadingMore = false;

                            if (isInitial) {
                                _uiState.setValue(ReaderUiState.success(chapter));

                                // If we have a saved page position for THIS chapter, scroll to it
                                // If we have a saved page position for THIS chapter, keep it ready for Fragment
                                if (savedChapterId == chapter.getId() && pendingScrollToRelativePage != null) {
                                    // Fragment will check and consume this via consumePendingScrollPosition()
                                }
                            } else {
                                // Signal fragment to append this chapter
                                _appendChapterEvent.setValue(chapter);
                                ReaderUiState current = _uiState.getValue();
                                if (current != null) {
                                    _uiState.setValue(current.withLoadingMore(false));
                                }
                            }
                        }, throwable -> {
                            isLoadingMore = false;
                            if (isInitial) {
                                _uiState.setValue(ReaderUiState.error(throwable.getMessage()));
                            }
                        })
        );
    }

    /**
     * Called by Fragment when user scrolls near the end.
     * Loads the next chapter if available.
     */
    public void loadNextChapterIfNeeded() {
        if (isLoadingMore || pendingNextChapterId == null) return;
        loadChapter(pendingNextChapterId, false);
    }

    /**
     * Save reading progress to local Room DB via debounced Subject.
     */
    public void saveReadingProgress(int chapterId, int pageIndex) {
        if (comicId <= 0 || chapterId <= 0 || pageIndex < 0) return;
        
        ReadingHistoryEntity entity = new ReadingHistoryEntity();
        entity.comicId = this.comicId;
        entity.chapterId = chapterId;
        entity.pageIndex = pageIndex;
        entity.readAt = System.currentTimeMillis();

        int percent = 0;
        ChapterEntity chapter = getChapterById(chapterId);
        if (chapter != null && chapter.getImages() != null && !chapter.getImages().isEmpty()) {
            int totalPages = chapter.getImages().size();
            percent = (int) (((pageIndex + 1) / (float) totalPages) * 100);
        }
        entity.percent = percent;

        // Push to subject for debounced processing
        saveProgressSubject.onNext(entity);
    }

    /**
     * Get the chapter entity that contains a given chapter ID
     */
    public ChapterEntity getChapterById(int chapterId) {
        for (ChapterEntity ch : loadedChapters) {
            if (ch.getId() == chapterId) return ch;
        }
        return null;
    }

    public boolean hasNextChapter() {
        return pendingNextChapterId != null;
    }

    public Integer consumePendingScrollPosition() {
        Integer value = pendingScrollToRelativePage;
        pendingScrollToRelativePage = null; // Consume
        return value;
    }

    public int getRestoredChapterId() {
        return savedChapterId;
    }

    public int getComicId() {
        return comicId;
    }

    /**
     * Save progress immediately asynchronously.
     * Used when Fragment is about to be destroyed or navigating away.
     */
    public void saveProgressImmediately(int chapterId, int pageIndex) {
        if (comicId <= 0 || chapterId <= 0) return;
        Log.d(TAG, ">>> ASYNC SAVING progress immediately: comicId=" + this.comicId + ", chapterId=" + chapterId + ", pageIndex=" + pageIndex);

        ReadingHistoryEntity entity = new ReadingHistoryEntity();
        entity.comicId = this.comicId;
        entity.chapterId = chapterId;
        entity.pageIndex = pageIndex;
        entity.readAt = System.currentTimeMillis();

        int percent = 0;
        ChapterEntity chapter = getChapterById(chapterId);
        if (chapter != null && chapter.getImages() != null && !chapter.getImages().isEmpty()) {
            int totalPages = chapter.getImages().size();
            percent = (int) (((pageIndex + 1) / (float) totalPages) * 100);
        }
        entity.percent = percent;

        // Fire and forget - not added to disposables so it completes even if ViewModel is cleared
        readingHistoryDao.insertOrUpdate(entity)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> Log.d(TAG, ">>> ASYNC SAVED OK: comicId=" + entity.comicId + ", page=" + pageIndex),
                        throwable -> Log.e(TAG, ">>> ASYNC SAVE FAILED: " + throwable.getMessage())
                );
    }

    public void trackChapterView(int chapterId) {
        if (comicId <= 0 || chapterId <= 0) return;
        disposables.add(
                apiService.trackView(new ViewTrackingRequest(comicId, chapterId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> Log.d(TAG, ">>> View tracking API SUCCESS for chapter: " + chapterId),
                                throwable -> Log.e(TAG, ">>> View tracking API FAILED: " + throwable.getMessage())
                        )
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
    }
}
