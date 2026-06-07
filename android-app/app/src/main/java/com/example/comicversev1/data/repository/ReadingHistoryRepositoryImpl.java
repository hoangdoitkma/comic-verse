package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.local.dao.ReadingHistoryDao;
import com.example.comicversev1.data.local.entity.ReadingHistoryEntity;
import com.example.comicversev1.domain.entity.HomeContent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class ReadingHistoryRepositoryImpl implements ReadingHistoryRepository {

    private final ReadingHistoryDao readingHistoryDao;

    @Inject
    public ReadingHistoryRepositoryImpl(ReadingHistoryDao readingHistoryDao) {
        this.readingHistoryDao = readingHistoryDao;
    }

    @Override
    public Flowable<List<HomeContent.ComicCard>> observeRecentCards(String contentType) {
        return readingHistoryDao.getRecentHistoryByType(contentType)
                .map(this::mapToCards);
    }

    @Override
    public Flowable<List<HomeContent.ComicCard>> observeAllCards(String contentType) {
        return readingHistoryDao.getAllHistoryByType(contentType)
                .map(this::mapToCards);
    }

    @Override
    public Single<ReadingHistoryEntity> getHistoryForComic(int comicId) {
        return readingHistoryDao.getHistoryForComic(comicId);
    }

    @Override
    public Completable deleteByComicId(int comicId) {
        return readingHistoryDao.deleteHistoryByComicId(comicId);
    }

    @Override
    public Completable clearAll() {
        return readingHistoryDao.deleteAllHistory();
    }

    private List<HomeContent.ComicCard> mapToCards(List<ReadingHistoryEntity> entities) {
        List<HomeContent.ComicCard> cards = new ArrayList<>();
        if (entities == null || entities.isEmpty()) {
            return cards;
        }

        for (ReadingHistoryEntity entity : entities) {
            cards.add(mapToCard(entity));
        }
        return cards;
    }

    private HomeContent.ComicCard mapToCard(ReadingHistoryEntity entity) {
        String title = entity.comicTitle != null ? entity.comicTitle : fallbackTitle(entity.comicType);
        String subtitle = entity.chapterTitle != null ? entity.chapterTitle : "ChÆ°Æ¡ng " + entity.chapterId;
        String slug = entity.slug != null ? entity.slug : "";
        String coverUrl = entity.coverUrl != null ? entity.coverUrl : "";
        String author = entity.authorName != null ? entity.authorName : "Äang cáº­p nháº­t";
        int progress = entity.percent > 0 ? entity.percent : 1;

        return new HomeContent.ComicCard(
                slug,
                title,
                subtitle,
                coverUrl,
                0,
                entity.viewCount,
                progress,
                "",
                "FREE",
                author
        );
    }

    private String fallbackTitle(String contentType) {
        return "NOVEL".equals(contentType) ? "Tiá»ƒu Thuyáº¿t" : "Truyá»‡n";
    }
}
