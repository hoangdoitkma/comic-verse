package com.datn.backend.repository;

import com.datn.backend.entity.ComicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComicGenreRepository extends JpaRepository<ComicGenre, Integer> {
    boolean existsByGenreId(Integer genreId);
}
