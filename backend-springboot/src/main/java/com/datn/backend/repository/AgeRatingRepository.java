package com.datn.backend.repository;

import com.datn.backend.entity.AgeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgeRatingRepository extends JpaRepository<AgeRating, Integer> {
}
