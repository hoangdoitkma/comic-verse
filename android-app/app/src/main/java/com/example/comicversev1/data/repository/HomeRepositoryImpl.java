package com.example.comicversev1.data.repository;

import com.example.comicversev1.R;
import com.example.comicversev1.domain.entity.HomeContent;

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

    @Inject
    public HomeRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
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
        return apiService.getHomeContent()
            .map(response -> {
                HomeDataResponse data = response.getData();
                if (data == null) data = new HomeDataResponse();
                return new HomeContent(
                        "Hi, Hàn Lập!",
                        "Chào mừng trở lại ✨",
                        Collections.emptyList(),
                        Arrays.asList(
                            new HomeContent.QuickAction("vip", "Trung tâm VIP", "Ưu đãi hội viên", R.drawable.ic_vip),
                            new HomeContent.QuickAction("remove_ads", "Xoá quảng cáo", "Tăng tốc đọc", R.drawable.ic_remove_ads),
                            new HomeContent.QuickAction("privacy", "Chính sách bảo mật", "Cập nhật mới", R.drawable.ic_policy),
                            new HomeContent.QuickAction("server", "Chọn máy chủ", "Tối ưu tốc độ", R.drawable.ic_server),
                            new HomeContent.QuickAction("download", "Tải xuống", "Đọc offline", R.drawable.ic_download),
                            new HomeContent.QuickAction("history", "Lịch sử", "Tiếp tục đọc", R.drawable.ic_history)
                        ),
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
