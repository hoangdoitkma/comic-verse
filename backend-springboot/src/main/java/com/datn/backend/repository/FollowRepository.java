package com.datn.backend.repository;

import com.datn.backend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    boolean existsByUserIdAndComicId(Integer userId, Integer comicId);
    Optional<Follow> findByUserIdAndComicId(Integer userId, Integer comicId);
    List<Follow> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
