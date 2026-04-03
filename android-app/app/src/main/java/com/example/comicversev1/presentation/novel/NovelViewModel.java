package com.example.comicversev1.presentation.novel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.repository.HomeRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;

@HiltViewModel
public class NovelViewModel extends ViewModel {

    private final MutableLiveData<NovelUiState> uiState = new MutableLiveData<>(NovelUiState.loading());
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final ReadingHistoryDao readingHistoryDao;
    private final HomeRepository repository;

    private final MutableLiveData<java.util.List<com.example.comicversev1.domain.entity.HomeContent.ComicCard>> _recentlyReadCards = new MutableLiveData<>(new java.util.ArrayList<>());
    public LiveData<java.util.List<com.example.comicversev1.domain.entity.HomeContent.ComicCard>> recentlyReadCards() { return _recentlyReadCards; }

    @Inject
    public NovelViewModel(HomeRepository repository, ReadingHistoryDao readingHistoryDao) {
        this.repository = repository;
        this.readingHistoryDao = readingHistoryDao;

        disposables.add(repository.loadNovelContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(content -> uiState.setValue(NovelUiState.from(content)),
                        throwable -> uiState.setValue(NovelUiState.error("Lỗi tải dữ liệu"))));

        loadReadingHistory();
    }

    public void refresh() {
        uiState.setValue(NovelUiState.loading());
        disposables.add(repository.loadNovelContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(content -> uiState.setValue(NovelUiState.from(content)),
                        throwable -> uiState.setValue(NovelUiState.error("Lỗi tải dữ liệu"))));
        loadReadingHistory();
    }

    private void loadReadingHistory() {
        disposables.add(readingHistoryDao.getRecentHistoryByType("NOVEL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(historyList -> {
                    if (historyList.isEmpty()) {
                        _recentlyReadCards.setValue(new java.util.ArrayList<>());
                        return;
                    }

                    java.util.List<com.example.comicversev1.domain.entity.HomeContent.ComicCard> cards = new java.util.ArrayList<>();
                    for (com.example.comicversev1.data.local.entity.ReadingHistoryEntity h : historyList) {
                        cards.add(new com.example.comicversev1.domain.entity.HomeContent.ComicCard(
                                h.slug != null ? h.slug : "",
                                h.comicTitle != null ? h.comicTitle : "Tiểu Thuyết",
                                h.chapterTitle != null ? h.chapterTitle : "Chương " + h.chapterId,
                                h.coverUrl != null ? h.coverUrl : "",
                                0,
                                h.viewCount,
                                h.percent > 0 ? h.percent : 1,
                                "",
                                "FREE",
                                h.authorName != null ? h.authorName : "Đang cập nhật"
                        ));
                    }
                    _recentlyReadCards.setValue(cards);
                }, throwable -> {
                    // Ignore or log error
                }));
    }

    public LiveData<NovelUiState> getUiState() {
        return uiState;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}

