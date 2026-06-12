package com.datn.backend.repository;

import com.datn.backend.entity.Notification;
import com.datn.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT * FROM notifications n WHERE n.id IN (" +
           "  SELECT MIN(n2.id) FROM notifications n2 " +
           "  GROUP BY IFNULL(n2.batch_id, CAST(n2.id AS CHAR))" +
           ") " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:isBroadcast IS NULL OR n.is_broadcast = :isBroadcast) " +
           "ORDER BY n.created_at DESC", 
           countQuery = "SELECT count(*) FROM notifications n WHERE n.id IN (" +
           "  SELECT MIN(n2.id) FROM notifications n2 " +
           "  GROUP BY IFNULL(n2.batch_id, CAST(n2.id AS CHAR))" +
           ") " +
           "AND (:type IS NULL OR n.type = :type) " +
           "AND (:isBroadcast IS NULL OR n.is_broadcast = :isBroadcast)", 
           nativeQuery = true)
    Page<Notification> findAdminGroupedHistory(@Param("type") String type, 
                                               @Param("isBroadcast") Boolean isBroadcast, 
                                               Pageable pageable);
}
