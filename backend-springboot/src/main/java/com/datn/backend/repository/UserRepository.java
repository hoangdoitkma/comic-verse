package com.datn.backend.repository;

import com.datn.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) AND (:status IS NULL OR u.status = :status)")
    org.springframework.data.domain.Page<User> findByRoleAndStatus(@org.springframework.data.repository.query.Param("role") com.datn.backend.entity.enums.Role role, @org.springframework.data.repository.query.Param("status") com.datn.backend.entity.enums.UserStatus status, org.springframework.data.domain.Pageable pageable);

    java.util.List<User> findByRole(com.datn.backend.entity.enums.Role role);

    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
