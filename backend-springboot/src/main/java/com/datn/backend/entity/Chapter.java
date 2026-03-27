package com.datn.backend.entity;

import com.datn.backend.entity.enums.AccessType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comic comic;

    @Column(name = "chapter_number", precision = 6, scale = 2)
    private BigDecimal chapterNumber;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 20)
    private AccessType accessType;

    @Column(name = "view_count")
    private Integer viewCount;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChapterPage> chapterPages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
