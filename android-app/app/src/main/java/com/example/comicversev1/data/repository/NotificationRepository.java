package com.example.comicversev1.data.repository;

import com.example.comicversev1.data.model.NotificationDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface NotificationRepository {
    Single<List<NotificationDTO>> getNotifications();

    Single<Long> getUnreadCount();

    Completable markAsRead(int notificationId);

    Completable markAllAsRead();
}
