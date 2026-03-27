package com.example.comicversev1.presentation.discover;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.example.comicversev1.data.remote.ComicPagingSource;
import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.domain.entity.ComicEntity;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class DiscoverViewModel extends ViewModel {
    private final LiveData<PagingData<ComicEntity>> pagingData;

    @Inject
    public DiscoverViewModel(ComicRepository repository) {
        CoroutineScope scope = ViewModelKt.getViewModelScope(this);
        pagingData = PagingLiveData.cachedIn(
                PagingLiveData.getLiveData(new Pager<>(new PagingConfig(20), () -> new ComicPagingSource(repository))),
                scope
        );
    }

    public LiveData<PagingData<ComicEntity>> pagingData() {
        return pagingData;
    }
}
