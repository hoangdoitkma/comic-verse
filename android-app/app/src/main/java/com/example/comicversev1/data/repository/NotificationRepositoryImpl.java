package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.api.ApiService;
import com.example.comicversev1.data.model.NotificationDTO;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class NotificationRepositoryImpl implements NotificationRepository {

    private final ApiService apiService;

    @Inject
    public NotificationRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<List<NotificationDTO>> getNotifications() {
        return apiService.getNotifications()
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    return Collections.<NotificationDTO>emptyList();
                });
    }

    @Override
    public Single<Long> getUnreadCount() {
        return apiService.getUnreadNotificationCount()
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData();
                    }
                    return 0L;
                });
    }

    @Override
    public Completable markAsRead(int notificationId) {
        return apiService.markNotificationAsRead(notificationId);
    }

    @Override
    public Completable markAllAsRead() {
        return apiService.markAllNotificationsAsRead();
    }
}
