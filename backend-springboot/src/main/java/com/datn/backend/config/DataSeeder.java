package com.datn.backend.config;

import com.datn.backend.entity.Genre;
import com.datn.backend.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;

    @Override
    public void run(String... args) throws Exception {
        seedGenres();
    }

    private void seedGenres() {
        if (genreRepository.count() == 0) {
            log.info("Seeding default genres to database...");
            List<String> defaultGenres = Arrays.asList(
                "Action", "Adventure", "Comedy", "Drama", "Fantasy", 
                "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life",
                "Sports", "Supernatural", "Thriller", "Isekai", "Harem",
                "Mecha", "Psychological", "School Life", "Shoujo", "Shounen",
                "Seinen", "Josei", "Martial Arts", "Historical", "Tragedy", "System", "Magic"
            );
            
            for (String name : defaultGenres) {
                if (!genreRepository.existsByName(name)) {
                    Genre genre = Genre.builder()
                        .name(name)
                        .description("Thể loại " + name)
                        .build();
                    genreRepository.save(genre);
                }
            }
            log.info("Seeded default genres successfully.");
        } else {
            log.info("Genres table already has data. Skipping DB seed.");
        }
    }
}
