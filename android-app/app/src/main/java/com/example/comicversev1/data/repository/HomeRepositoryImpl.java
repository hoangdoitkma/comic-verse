package com.example.comicversev1.data.repository;

import android.content.SharedPreferences;

import com.example.comicversev1.R;
import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.HomeDataResponse;
import com.example.comicversev1.domain.entity.HomeContent;
import com.example.comicversev1.utils.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class HomeRepositoryImpl implements HomeRepository {

    private final ApiService apiService;
    private final SharedPreferences prefs;

    @Inject
    public HomeRepositoryImpl(ApiService apiService, SharedPreferences prefs) {
        this.apiService = apiService;
        this.prefs = prefs;
    }

    private List<HomeContent.ComicCard> mapToCards(List<com.example.comicversev1.data.model.ComicDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(dto -> new HomeContent.ComicCard(
                dto.getSlug() != null ? dto.getSlug() : "",
                dto.getTitle() != null ? dto.getTitle() : "",
                dto.getTotalChapters() > 0 ? "Chương " + dto.getTotalChapters() : "Đang cập nhật",
                dto.getCoverImage(),
                0,
                dto.getViewCount(),
                0,
                "",
                dto.getAccessType() != null ? dto.getAccessType() : "FREE",
                ""
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
                .map(response -> response.getData() != null ? response.getData() : new HomeDataResponse());

        if (token == null || token.isEmpty()) {
            return homeDataSingle.map(data -> buildHomeContent(data, type, Collections.emptyList(), ""));
        }

        Single<List<com.example.comicversev1.data.model.ComicDTO>> recommendationsSingle =
                apiService.getRecommendations(type)
                        .map(response -> {
                            if (response.isSuccess() && response.getData() != null) {
                                return response.getData();
                            }
                            return Collections.<com.example.comicversev1.data.model.ComicDTO>emptyList();
                        })
                        .onErrorReturnItem(Collections.emptyList());

        Single<String> displayNameSingle = fetchUserDisplayName();

        return Single.zip(homeDataSingle, recommendationsSingle, displayNameSingle,
                (data, recs, displayName) -> buildHomeContent(data, type, recs, displayName));
    }

    private Single<String> fetchUserDisplayName() {
        String cachedName = prefs.getString(Constants.KEY_DISPLAY_NAME, "");
        return apiService.getUserProfile()
                .map(profileRes -> {
                    if (profileRes.isSuccess() && profileRes.getData() != null) {
                        String displayName = profileRes.getData().displayName;
                        String avatarUrl = profileRes.getData().avatarUrl;
                        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                            prefs.edit().putString(Constants.KEY_AVATAR_URL, avatarUrl.trim()).apply();
                        }
                        if (displayName != null && !displayName.trim().isEmpty()) {
                            String normalizedName = displayName.trim();
                            prefs.edit().putString(Constants.KEY_DISPLAY_NAME, normalizedName).apply();
                            return normalizedName;
                        }
                    }
                    return cachedName != null ? cachedName : "";
                })
                .onErrorReturnItem(cachedName != null ? cachedName : "");
    }

    private HomeContent buildHomeContent(HomeDataResponse data,
                                         String type,
                                         List<com.example.comicversev1.data.model.ComicDTO> personalizedRecs,
                                         String displayName) {
        String name = displayName != null && !displayName.trim().isEmpty()
                ? displayName.trim()
                : prefs.getString(Constants.KEY_DISPLAY_NAME, "");
        String greeting = name == null || name.trim().isEmpty() ? "Hi, Khách!" : "Hi, " + name.trim() + "!";

        List<HomeContent.ComicCard> recommendedCards;
        if (personalizedRecs != null && !personalizedRecs.isEmpty()) {
            recommendedCards = mapToCards(personalizedRecs);
        } else {
            recommendedCards = mapToCards(data.recommended);
        }

        return new HomeContent(
                greeting,
                "Chào mừng trở lại",
                Collections.emptyList(),
                "COMIC".equals(type) ? Arrays.asList(
                        new HomeContent.QuickAction("vip", "Mua gói VIP", "Ưu đãi hội viên", R.drawable.ic_vip),
                        new HomeContent.QuickAction("history", "Lịch sử", "Tiếp tục đọc", R.drawable.ic_history)
                ) : Collections.emptyList(),
                null,
                Collections.emptyList(),
                recommendedCards,
                mapToCards(data.recentlyUpdated),
                mapToCards(data.topTrending),
                Collections.emptyList(),
                mapToCards(data.newComics)
        );
    }
}
