package com.datn.backend.repository;

import com.datn.backend.entity.PasswordResetToken;
import com.datn.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);
    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
