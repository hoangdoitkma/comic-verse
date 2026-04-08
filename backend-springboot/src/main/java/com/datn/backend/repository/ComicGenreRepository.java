package com.datn.backend.repository;

import com.datn.backend.entity.ComicGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComicGenreRepository extends JpaRepository<ComicGenre, Integer> {
    boolean existsByGenreId(Integer genreId);
    List<ComicGenre> findByComicIdIn(List<Integer> comicIds);
    List<ComicGenre> findByGenreIdIn(List<Integer> genreIds);
}
