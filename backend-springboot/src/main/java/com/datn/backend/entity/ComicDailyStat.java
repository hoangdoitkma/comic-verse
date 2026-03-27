package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comic_daily_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComicDailyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "stat_date")
    private LocalDate statDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id")
    private Comic comic;

    @Column(name = "total_views")
    private Integer totalViews;

    @Column(name = "unique_viewers")
    private Integer uniqueViewers;

    @Column(name = "total_follows")
    private Integer totalFollows;

    @Column(name = "new_follows")
    private Integer newFollows;

    @Column(name = "total_likes")
    private Integer totalLikes;

    @Column(name = "total_comments")
    private Integer totalComments;

    @Column(name = "total_revenue", precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
