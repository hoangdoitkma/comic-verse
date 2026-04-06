package com.example.comicversev1.presentation.history;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
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

    private final ReadingHistoryDao historyDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<HomeContent.ComicCard>> _comicHistory = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> comicHistory() { return _comicHistory; }

    private final MutableLiveData<List<HomeContent.ComicCard>> _novelHistory = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> novelHistory() { return _novelHistory; }

    @Inject
    public HistoryViewModel(ReadingHistoryDao historyDao) {
        this.historyDao = historyDao;
        loadHistory();
    }

    private void loadHistory() {
        // Observe Comic History
        disposables.add(
                historyDao.getAllHistoryByType("COMIC")
                        .subscribeOn(Schedulers.io())
                        .map(entities -> {
                            List<HomeContent.ComicCard> cards = new ArrayList<>();
                            for (com.example.comicversev1.data.local.entity.ReadingHistoryEntity e : entities) {
                                String subtitle = e.chapterTitle != null ? e.chapterTitle : "Chương " + e.chapterId;
                                cards.add(new HomeContent.ComicCard(e.slug, e.comicTitle, subtitle, e.coverUrl, 0, e.viewCount, e.percent, "", "FREE", e.authorName));
                            }
                            return cards;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(_comicHistory::setValue,
                                throwable -> Log.e("HistoryVM", "Error loading comic history", throwable))
        );

        // Observe Novel History
        disposables.add(
                historyDao.getAllHistoryByType("NOVEL")
                        .subscribeOn(Schedulers.io())
                        .map(entities -> {
                            List<HomeContent.ComicCard> cards = new ArrayList<>();
                            for (com.example.comicversev1.data.local.entity.ReadingHistoryEntity e : entities) {
                                String subtitle = e.chapterTitle != null ? e.chapterTitle : "Chương " + e.chapterId;
                                cards.add(new HomeContent.ComicCard(e.slug, e.comicTitle, subtitle, e.coverUrl, 0, e.viewCount, e.percent, "", "FREE", e.authorName));
                            }
                            return cards;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(_novelHistory::setValue,
                                throwable -> Log.e("HistoryVM", "Error loading novel history", throwable))
        );
    }

    public void clearAllHistory() {
        disposables.add(
                historyDao.deleteAllHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d("HistoryVM", "All reading history cleared");
                        }, throwable -> {
                            Log.e("HistoryVM", "Failed to clear history", throwable);
                        })
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
