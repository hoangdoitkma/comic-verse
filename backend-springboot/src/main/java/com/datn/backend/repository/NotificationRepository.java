package com.datn.backend.repository;

import com.datn.backend.entity.Notification;
import com.datn.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Get notifications for a specific user, ordered by creation date desc
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Get unread notification count
    long countByUserAndIsReadFalse(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadByUser(User user);
}
