package com.datn.backend.repository;

import com.datn.backend.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    boolean existsByName(String name);
}
