package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;

import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.HomeContent;
import com.example.comicversev1.utils.Constants;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.HomeDataResponse;
import java.util.Collections;

@Singleton
public class HomeRepositoryImpl implements HomeRepository {

    private final ApiService apiService;
    private final SharedPreferences prefs;

    @Inject
    public HomeRepositoryImpl(ApiService apiService, SharedPreferences prefs) {
        this.apiService = apiService;
        this.prefs = prefs;
    }

    private java.util.List<HomeContent.ComicCard> mapToCards(java.util.List<com.example.comicversev1.data.model.ComicDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(dto -> new HomeContent.ComicCard(
                dto.getSlug() != null ? dto.getSlug() : "",
                dto.getTitle() != null ? dto.getTitle() : "",
                "Chương " + dto.getTotalChapters(),
                dto.getCoverImage(),
                0, // fallback likes
                dto.getViewCount(),
                0, // fallback progress
                "" // fallback timeLabel
        )).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Single<HomeContent> loadHomeContent() {
        return fetchContent("COMIC");
    }

    @Override
    public Single<HomeContent> loadNovelContent() {
        return fetchContent("NOVEL");
    }

    private Single<HomeContent> fetchContent(String type) {
        return apiService.getHomeContent(type)
            .map(response -> {
                HomeDataResponse data = response.getData();
                if (data == null) data = new HomeDataResponse();
                
                String name = prefs.getString(Constants.KEY_DISPLAY_NAME, "");
                String greeting = name.isEmpty() ? "Hi, Khách!" : "Hi, " + name + "!";

                return new HomeContent(
                        greeting,
                        "Chào mừng trở lại ✨",
                        Collections.emptyList(),
                        "COMIC".equals(type) ? Arrays.asList(
                            new HomeContent.QuickAction("vip", "Trung tâm VIP", "Ưu đãi hội viên", R.drawable.ic_vip),
                            new HomeContent.QuickAction("remove_ads", "Xoá quảng cáo", "Tăng tốc đọc", R.drawable.ic_remove_ads),
                            new HomeContent.QuickAction("history", "Lịch sử", "Tiếp tục đọc", R.drawable.ic_history)
                        ) : Collections.emptyList(),
                        null,
                        mapToCards(data.recentlyUpdated),
                        mapToCards(data.recommended),
                        mapToCards(data.recentlyUpdated),
                        mapToCards(data.topTrending),
                        Collections.emptyList(),
                        mapToCards(data.newComics)
                );
            });
    }
}
