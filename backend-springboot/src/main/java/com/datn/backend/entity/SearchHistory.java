package com.datn.backend.entity;

import com.datn.backend.entity.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_history", indexes = {
        @Index(name = "idx_search_history_user_time", columnList = "user_id,searched_at"),
        @Index(name = "idx_search_history_keyword_time", columnList = "normalized_keyword,searched_at"),
        @Index(name = "idx_search_history_type_time", columnList = "content_type,searched_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 255)
    private String keyword;

    @Column(name = "normalized_keyword", length = 255)
    private String normalizedKeyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 10)
    private ContentType contentType;

    @CreationTimestamp
    @Column(name = "searched_at", updatable = false)
    private LocalDateTime searchedAt;
}
