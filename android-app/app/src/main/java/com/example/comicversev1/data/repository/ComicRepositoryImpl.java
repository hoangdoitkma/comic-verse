package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.local.dao.ComicCacheDao;
import com.example.comicversev1.data.local.entity.ComicCacheEntity;
import com.example.comicversev1.data.model.BaseResponse;
import com.example.comicversev1.data.model.ChapterDetailDTO;
import com.example.comicversev1.data.model.ComicDTO;
import com.example.comicversev1.data.model.ComicDetailDTO;
import com.example.comicversev1.data.model.ChapterItemDTO;
import com.example.comicversev1.domain.entity.ChapterEntity;
import com.example.comicversev1.domain.entity.ComicEntity;
import com.example.comicversev1.domain.entity.ComicDetailEntity;
import com.example.comicversev1.domain.entity.ChapterItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class ComicRepositoryImpl implements ComicRepository {

    private final ApiService apiService;
    private final ComicCacheDao cacheDao;

    @Inject
    public ComicRepositoryImpl(ApiService apiService, ComicCacheDao cacheDao) {
        this.apiService = apiService;
        this.cacheDao = cacheDao;
    }

    @Override
    public Single<List<ComicEntity>> getComics(int page, int limit) {
        return apiService.getComics(page, limit)
                .flatMap(response -> {
                    List<ComicEntity> comics = mapComics(response.getData());
                    return cacheComics(comics).andThen(Single.just(comics));
                });
    }

    @Override
    public Single<ComicDetailEntity> getComicDetail(String slug) {
        return apiService.getComicDetail(slug).map(response -> mapComicDetail(response.getData()));
    }

    @Override
    public Single<List<ChapterItem>> getChapters(String slug) {
        return apiService.getChapters(slug).map(resp -> mapChapterItems(resp.getData()));
    }

    @Override
    public Single<ChapterEntity> getChapterDetail(int chapterId) {
        return apiService.getChapterContent(chapterId).map(response -> mapChapter(response.getData()));
    }

    @Override
    public Flowable<List<ComicEntity>> observeCachedComics() {
        return cacheDao.observeAll().map(this::mapComicCacheEntities);
    }

    private Completable cacheComics(List<ComicEntity> comics) {
        List<ComicCacheEntity> entities = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (ComicEntity c : comics) {
            ComicCacheEntity e = new ComicCacheEntity();
            e.comicId = c.getId();
            e.slug = c.getSlug();
            e.title = c.getTitle();
            e.coverImage = c.getCoverImage();
            e.updatedAt = now;
            entities.add(e);
        }
        return cacheDao.clear().andThen(cacheDao.upsertAll(entities));
    }

    private List<ComicEntity> mapComics(List<ComicDTO> dtos) {
        List<ComicEntity> list = new ArrayList<>();
        if (dtos == null) return list;
        for (ComicDTO dto : dtos) {
            list.add(mapComic(dto));
        }
        return list;
    }

    private ComicEntity mapComic(ComicDTO dto) {
        if (dto == null) return new ComicEntity(0, "", "", "");
        return new ComicEntity(dto.getId(), dto.getSlug(), dto.getTitle(), dto.getCoverImage());
    }

    private ChapterEntity mapChapter(ChapterDetailDTO dto) {
        if (dto == null) return new ChapterEntity(0, "", new ArrayList<>());
        return new ChapterEntity(dto.getId(), dto.getTitle(), dto.getChapterNum(),
                dto.getImages() != null ? dto.getImages() : new ArrayList<>(),
                dto.getNextChapterId(), dto.getPrevChapterId());
    }

    private List<ComicEntity> mapComicCacheEntities(List<ComicCacheEntity> entities) {
        List<ComicEntity> list = new ArrayList<>();
        if (entities == null) return list;
        for (ComicCacheEntity e : entities) {
            list.add(new ComicEntity(e.comicId, e.slug, e.title, e.coverImage));
        }
        return list;
    }

    private ComicDetailEntity mapComicDetail(ComicDetailDTO dto) {
        if (dto == null) return new ComicDetailEntity(0, "", "", "", "");
        return new ComicDetailEntity(dto.getId(), dto.getSlug(), dto.getTitle(), dto.getCoverImage(), dto.getAiSummary());
    }

    private List<ChapterItem> mapChapterItems(List<ChapterItemDTO> dtos) {
        List<ChapterItem> list = new ArrayList<>();
        if (dtos == null) return list;
        for (ChapterItemDTO dto : dtos) {
            list.add(new ChapterItem(dto.getId(), dto.getTitle(), dto.getAccessType()));
        }
        return list;
    }
}
