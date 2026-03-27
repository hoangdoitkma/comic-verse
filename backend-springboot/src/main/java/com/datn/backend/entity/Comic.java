package com.datn.backend.entity;

import com.datn.backend.entity.enums.ComicFormat;
import com.datn.backend.entity.enums.ComicStatus;
import com.datn.backend.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "age_rating_id")
    private AgeRating ageRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 10)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "comic_format", length = 15)
    private ComicFormat comicFormat;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ComicStatus status;

    @Column(name = "total_chapters")
    private Integer totalChapters;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
