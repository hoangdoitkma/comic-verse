package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comic_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComicGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
