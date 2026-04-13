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
import java.util.List;

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
                dto.getTotalChapters() > 0 ? "Chương " + dto.getTotalChapters() : "Đang cập nhật",
                dto.getCoverImage(),
                0, // fallback likes
                dto.getViewCount(),
                0, // fallback progress
                "", // fallback timeLabel
                dto.getAccessType() != null ? dto.getAccessType() : "FREE",
                "" // fallback authorName
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
    
    @Override
    public Single<List<HomeContent.ComicCard>> getSimilarComics(String slug) {
        return apiService.getSimilarComics(slug)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return mapToCards(response.getData());
                    }
                    return Collections.<HomeContent.ComicCard>emptyList();
                })
                .onErrorReturnItem(Collections.<HomeContent.ComicCard>emptyList());
    }

    private Single<HomeContent> fetchContent(String type) {
        String token = prefs.getString(Constants.KEY_ACCESS_TOKEN, "");

        Single<HomeDataResponse> homeDataSingle = apiService.getHomeContent(type)
                .map(response -> {
                    if (response.getData() != null) return response.getData();
                    return new HomeDataResponse();
                });

        if (token.isEmpty()) {
            return homeDataSingle.map(data -> buildHomeContent(data, type, Collections.emptyList()));
        } else {
            // Khi đã login: gọi song song home data + recommendations + user profile
            Single<List<com.example.comicversev1.data.model.ComicDTO>> recommendationsSingle = 
                apiService.getRecommendations(type)
                    .map(response -> {
                        if (response.isSuccess() && response.getData() != null) {
                            return response.getData();
                        }
                        return Collections.<com.example.comicversev1.data.model.ComicDTO>emptyList();
                    })
                    .onErrorReturnItem(Collections.emptyList());

            return Single.zip(homeDataSingle, recommendationsSingle, (data, recs) -> {
                // Fetch user profile name (fire and forget style, cached in prefs)
                fetchUserProfileAsync();
                return buildHomeContent(data, type, recs);
            });
        }
    }

    /**
     * Fire-and-forget: Lấy user profile và cache display name vào SharedPreferences
     */
    private void fetchUserProfileAsync() {
        try {
            apiService.getUserProfile()
                .subscribe(profileRes -> {
                    if (profileRes.isSuccess() && profileRes.getData() != null) {
                        String displayName = profileRes.getData().displayName;
                        if (displayName != null && !displayName.isEmpty()) {
                            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, displayName).apply();
                        }
                    }
                }, error -> { /* Silently ignore */ });
        } catch (Exception e) {
            // Ignore
        }
    }

    private HomeContent buildHomeContent(HomeDataResponse data, String type,
                                          List<com.example.comicversev1.data.model.ComicDTO> personalizedRecs) {
        String name = prefs.getString(Constants.KEY_DISPLAY_NAME, "");
        String greeting = name.isEmpty() ? "Hi, Khách!" : "Hi, " + name + "!";

        // Nếu có kết quả đề xuất cá nhân hóa → dùng, nếu không → fallback sang recommended từ home API
        List<HomeContent.ComicCard> recommendedCards;
        if (personalizedRecs != null && !personalizedRecs.isEmpty()) {
            recommendedCards = mapToCards(personalizedRecs);
        } else {
            recommendedCards = mapToCards(data.recommended);
        }

        return new HomeContent(
                greeting,
                "Chào mừng trở lại ✨",
                Collections.emptyList(),
                "COMIC".equals(type) ? Arrays.asList(
                    new HomeContent.QuickAction("vip", "Mua gói VIP", "Ưu đãi hội viên", R.drawable.ic_vip),
                    new HomeContent.QuickAction("remove_ads", "Xoá QC", "Tăng tốc đọc", R.drawable.ic_remove_ads),
                    new HomeContent.QuickAction("history", "Lịch sử", "Tiếp tục đọc", R.drawable.ic_history)
                ) : Collections.emptyList(),
                null,
                mapToCards(data.recentlyUpdated),
                recommendedCards,
                mapToCards(data.recentlyUpdated),
                mapToCards(data.topTrending),
                Collections.emptyList(),
                mapToCards(data.newComics)
        );
    }
}

