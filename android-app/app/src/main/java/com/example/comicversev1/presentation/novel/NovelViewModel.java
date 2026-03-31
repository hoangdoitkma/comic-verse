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

@HiltViewModel
public class NovelViewModel extends ViewModel {

    private final MutableLiveData<NovelUiState> uiState = new MutableLiveData<>(NovelUiState.loading());
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public NovelViewModel(HomeRepository repository) {
        disposables.add(repository.loadNovelContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(content -> uiState.setValue(NovelUiState.from(content)),
                        throwable -> uiState.setValue(NovelUiState.error("Lỗi tải dữ liệu"))));
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

