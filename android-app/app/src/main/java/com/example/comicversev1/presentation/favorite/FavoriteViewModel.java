package com.example.comicversev1.presentation.favorite;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.comicversev1.data.repository.FavoriteSyncRepository;
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

    private static final String TAG = "FavoriteVM";

    private final FavoriteSyncRepository favoriteSyncRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<List<HomeContent.ComicCard>> comicFavorites = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<HomeContent.ComicCard>> novelFavorites = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);

    @Inject
    public FavoriteViewModel(FavoriteSyncRepository favoriteSyncRepository) {
        this.favoriteSyncRepository = favoriteSyncRepository;
        loadFavorites();
        syncFavorites();
    }

    public LiveData<List<HomeContent.ComicCard>> comicFavorites() {
        return comicFavorites;
    }

    public LiveData<List<HomeContent.ComicCard>> novelFavorites() {
        return novelFavorites;
    }

    public LiveData<Boolean> refreshing() {
        return refreshing;
    }

    public void refresh() {
        refreshing.setValue(true);
        syncFavorites(true);
    }

    private void loadFavorites() {
        disposables.add(favoriteSyncRepository.observeFavoriteCards("COMIC")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comicFavorites::setValue,
                        throwable -> Log.e(TAG, "Error loading comic favorites", throwable)));

        disposables.add(favoriteSyncRepository.observeFavoriteCards("NOVEL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(novelFavorites::setValue,
                        throwable -> Log.e(TAG, "Error loading novel favorites", throwable)));
    }

    private void syncFavorites() {
        syncFavorites(false);
    }

    private void syncFavorites(boolean trackRefresh) {
        disposables.add(favoriteSyncRepository.syncWithServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d(TAG, "Favorite sync completed");
                            if (trackRefresh) refreshing.setValue(false);
                        },
                        throwable -> {
                            Log.e(TAG, "Favorite sync skipped/failed", throwable);
                            if (trackRefresh) refreshing.setValue(false);
                        }
                ));
    }

    @Override
    protected void onCleared() {
        disposables.clear();
        super.onCleared();
    }
}
