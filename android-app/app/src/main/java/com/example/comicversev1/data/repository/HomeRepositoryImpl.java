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

    @Override
    public Single<HomeContent> loadHomeContent() {
        return apiService.getHomeContent()
            .map(response -> {
                HomeDataResponse data = response.getData();
                return new HomeContent(
                        "Hi, Hàn Lập!",
                        "Chào mừng trở lại ✨",
                        data.heroes != null ? data.heroes : Collections.emptyList(),
                        Arrays.asList(
                            new HomeContent.QuickAction("vip", "Trung tâm VIP", "Ưu đãi hội viên", R.drawable.ic_vip),
                            new HomeContent.QuickAction("remove_ads", "Xoá quảng cáo", "Tăng tốc đọc", R.drawable.ic_remove_ads),
                            new HomeContent.QuickAction("privacy", "Chính sách bảo mật", "Cập nhật mới", R.drawable.ic_policy),
                            new HomeContent.QuickAction("server", "Chọn máy chủ", "Tối ưu tốc độ", R.drawable.ic_server),
                            new HomeContent.QuickAction("download", "Tải xuống", "Đọc offline", R.drawable.ic_download),
                            new HomeContent.QuickAction("history", "Lịch sử", "Tiếp tục đọc", R.drawable.ic_history)
                        ),
                        null,
                        data.recent != null ? data.recent : Collections.emptyList(),
                        data.recommendations != null ? data.recommendations : Collections.emptyList(),
                        data.newUpdates != null ? data.newUpdates : Collections.emptyList(),
                        data.hotComics != null ? data.hotComics : Collections.emptyList(),
                        data.completed != null ? data.completed : Collections.emptyList(),
                        data.newComics != null ? data.newComics : Collections.emptyList()
                );
            });
    }
}
