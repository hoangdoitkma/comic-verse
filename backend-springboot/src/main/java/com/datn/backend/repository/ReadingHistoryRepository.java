package com.datn.backend.repository;

import com.datn.backend.entity.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Integer> {
    Optional<ReadingHistory> findByUserIdAndComicId(Integer userId, Integer comicId);
    List<ReadingHistory> findByUserIdAndComicIdIn(Integer userId, List<Integer> comicIds);
}
