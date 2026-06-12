package com.example.comicversev1.data.remote;

import androidx.annotation.NonNull;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.example.comicversev1.data.repository.ComicRepository;
import com.example.comicversev1.domain.entity.ComicEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ComicPagingSource extends RxPagingSource<Integer, ComicEntity> {

    private final ComicRepository repository;
    private final String keyword;
    private final String type;
    private final String country;
    private final Integer genreId;
    private final String status;

    public ComicPagingSource(ComicRepository repository, String keyword, String type) {
        this(repository, keyword, type, null, null, null);
    }

    public ComicPagingSource(ComicRepository repository, String keyword, String type, String country, Integer genreId, String status) {
        this.repository = repository;
        this.keyword = keyword;
        this.type = type;
        this.country = country;
        this.genreId = genreId;
        this.status = status;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, ComicEntity>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        int page = loadParams.getKey() != null ? loadParams.getKey() : 0;
        return repository.getComics(page, 20, keyword, type, country, genreId, status)
                .subscribeOn(Schedulers.io())
                .map(comics -> toLoadResult(comics, page))
                .onErrorReturn(LoadResult.Error::new);
    }

    private LoadResult<Integer, ComicEntity> toLoadResult(List<ComicEntity> data, int page) {
        Integer prevKey = page == 0 ? null : page - 1;
        Integer nextKey = data.isEmpty() ? null : page + 1;
        return new LoadResult.Page<>(data, prevKey, nextKey);
    }

    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, ComicEntity> state) {
        return null;
    }
}
