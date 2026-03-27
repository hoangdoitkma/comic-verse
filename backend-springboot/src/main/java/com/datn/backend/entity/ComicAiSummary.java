package com.datn.backend.entity;

import com.datn.backend.entity.enums.SummarySource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comic_ai_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComicAiSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_source", length = 20)
    private SummarySource summarySource;

    @Column(name = "generated_from_chapters")
    private Integer generatedFromChapters;

    @Column(length = 50)
    private String model;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
