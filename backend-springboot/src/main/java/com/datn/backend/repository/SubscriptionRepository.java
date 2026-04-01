package com.datn.backend.repository;

import com.datn.backend.entity.Subscription;
import com.datn.backend.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    List<Subscription> findByUserIdAndStatus(Integer userId, SubscriptionStatus status);
}
