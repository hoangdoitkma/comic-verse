package com.example.comicversev1.presentation.favorite;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.local.dao.FavoriteComicDao;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class FavoriteViewModel extends ViewModel {

    private final FavoriteComicDao favoriteComicDao;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<HomeContent.ComicCard>> _comicFavorites = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> comicFavorites() { return _comicFavorites; }

    private final MutableLiveData<List<HomeContent.ComicCard>> _novelFavorites = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<HomeContent.ComicCard>> novelFavorites() { return _novelFavorites; }

    @Inject
    public FavoriteViewModel(FavoriteComicDao favoriteComicDao) {
        this.favoriteComicDao = favoriteComicDao;
        loadFavorites();
    }

    private void loadFavorites() {
        // Observe Comic Favorites
        disposables.add(
                favoriteComicDao.getAllFavoritesByType("COMIC")
                        .subscribeOn(Schedulers.io())
                        .map(entities -> {
                            List<HomeContent.ComicCard> cards = new ArrayList<>();
                            for (com.example.comicversev1.data.local.entity.FavoriteComicEntity e : entities) {
                                cards.add(new HomeContent.ComicCard(e.slug, e.comicTitle, "", e.coverUrl, 0, 0, 0, "", "FREE", ""));
                            }
                            return cards;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(_comicFavorites::setValue,
                                throwable -> Log.e("FavoriteVM", "Error loading comic favorites", throwable))
        );

        // Observe Novel Favorites
        disposables.add(
                favoriteComicDao.getAllFavoritesByType("NOVEL")
                        .subscribeOn(Schedulers.io())
                        .map(entities -> {
                            List<HomeContent.ComicCard> cards = new ArrayList<>();
                            for (com.example.comicversev1.data.local.entity.FavoriteComicEntity e : entities) {
                                cards.add(new HomeContent.ComicCard(e.slug, e.comicTitle, "", e.coverUrl, 0, 0, 0, "", "FREE", ""));
                            }
                            return cards;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(_novelFavorites::setValue,
                                throwable -> Log.e("FavoriteVM", "Error loading novel favorites", throwable))
        );
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
