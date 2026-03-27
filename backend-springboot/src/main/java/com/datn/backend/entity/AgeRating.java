package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "age_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String description;
}
