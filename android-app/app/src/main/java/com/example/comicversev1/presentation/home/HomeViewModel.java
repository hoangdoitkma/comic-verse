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

    // Separate LiveData for "Bạn vừa đọc" section (from local Room DB + API info)
    private final MutableLiveData<List<HomeContent.ComicCard>> _recentlyReadCards = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> recentlyReadCards() { return _recentlyReadCards; }

    @Inject
    public HomeViewModel(HomeRepository repository, ReadingHistoryDao readingHistoryDao, ApiService apiService) {
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

    /**
     * Load reading history from Room DB, then call API to get comic info
     */
    private void loadReadingHistory() {
        disposables.add(readingHistoryDao.getRecentHistory()
                .firstOrError() // Take the first emission from Flowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(historyList -> {
                    if (historyList.isEmpty()) {
                        _recentlyReadCards.setValue(new ArrayList<>());
                        return;
                    }

                    // Build API request
                    List<ReadingHistoryInfoRequest.Item> items = new ArrayList<>();
                    for (ReadingHistoryEntity h : historyList) {
                        items.add(new ReadingHistoryInfoRequest.Item(h.comicId, h.chapterId, h.pageIndex));
                    }
                    ReadingHistoryInfoRequest request = new ReadingHistoryInfoRequest(items);

                    // Fetch comic info from API
                    disposables.add(apiService.getReadingHistoryInfo(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {
                                if (response.isSuccess() && response.getData() != null) {
                                    List<HomeContent.ComicCard> cards = new ArrayList<>();
                                    for (ReadingHistoryInfoDTO dto : response.getData()) {
                                        cards.add(new HomeContent.ComicCard(
                                                dto.slug,
                                                dto.title,
                                                dto.chapterTitle,
                                                dto.coverUrl,
                                                dto.likes,
                                                dto.views,
                                                dto.progress,
                                                "", // timeLabel N/A
                                                "FREE"
                                        ));
                                    }
                                    _recentlyReadCards.setValue(cards);
                                    Log.d(TAG, "Loaded " + cards.size() + " reading history cards");
                                }
                            }, throwable -> {
                                Log.e(TAG, "Failed to load reading history info: " + throwable.getMessage());
                            }));
                }, throwable -> {
                    Log.d(TAG, "No reading history found");
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
