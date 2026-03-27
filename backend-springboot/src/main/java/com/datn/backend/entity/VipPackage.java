package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vip_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String name;

    @Column(name = "duration_month")
    private Integer durationMonth;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String currency;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
