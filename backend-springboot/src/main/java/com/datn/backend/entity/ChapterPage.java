package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chapter_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
}
