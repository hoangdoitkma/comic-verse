package com.example.comicversev1.presentation.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.data.model.ReadingHistoryInfoDTO;
import com.example.comicversev1.data.model.ReadingHistoryInfoRequest;
import com.example.comicversev1.data.repository.HomeRepository;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<HomeUiState> uiState = new MutableLiveData<>(HomeUiState.loading());
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final ReadingHistoryDao readingHistoryDao;
    private final ApiService apiService;
    private final HomeRepository repository;

    // Separate LiveData for "Bạn vừa đọc" section (from local Room DB + API info)
    private final MutableLiveData<List<HomeContent.ComicCard>> _recentlyReadCards = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> recentlyReadCards() { return _recentlyReadCards; }

    @Inject
    public HomeViewModel(HomeRepository repository, ReadingHistoryDao readingHistoryDao, ApiService apiService) {
        this.repository = repository;
        this.readingHistoryDao = readingHistoryDao;
        this.apiService = apiService;

        // Load home content from API
        disposables.add(repository.loadHomeContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        content -> uiState.setValue(HomeUiState.from(content)),
                        throwable -> uiState.setValue(HomeUiState.error("API Error: " + throwable.getMessage()))
                ));

        // Load local reading history and enrich with API data
        loadReadingHistory();
    }

    public void refresh() {
        uiState.setValue(HomeUiState.loading());
        disposables.add(repository.loadHomeContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        content -> uiState.setValue(HomeUiState.from(content)),
                        throwable -> uiState.setValue(HomeUiState.error("API Error: " + throwable.getMessage()))
                ));
        loadReadingHistory();
    }

    /**
     * Tải Lịch sử trực tiếp từ Room DB local (Không qua mạng để đạt tốc độ hiển thị 0ms và Offline 100%)
     * Luồng Flowable sẽ auto-trigger UI mỗi khi người dùng đọc và update DB
     */
    private void loadReadingHistory() {
        disposables.add(readingHistoryDao.getRecentHistoryByType("COMIC")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(historyList -> {
                    if (historyList.isEmpty()) {
                        _recentlyReadCards.setValue(new ArrayList<>());
                        return;
                    }

                    List<HomeContent.ComicCard> cards = new ArrayList<>();
                    for (ReadingHistoryEntity h : historyList) {
                        cards.add(new HomeContent.ComicCard(
                                h.slug != null ? h.slug : "",
                                h.comicTitle != null ? h.comicTitle : "Truyện",
                                h.chapterTitle != null ? h.chapterTitle : "Chương " + h.chapterId,
                                h.coverUrl != null ? h.coverUrl : "",
                                0, // Offline không lấy được realtime likes
                                h.viewCount,
                                h.percent > 0 ? h.percent : 1, // Progress
                                "", // timeLabel
                                "FREE",
                                h.authorName != null ? h.authorName : "Đang cập nhật"
                        ));
                    }
                    _recentlyReadCards.setValue(cards);
                    Log.d(TAG, "Rendered " + cards.size() + " reading history cards securely locally!");
                }, throwable -> {
                    Log.e(TAG, "Failed to load local reading history: " + throwable.getMessage());
                }));
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
