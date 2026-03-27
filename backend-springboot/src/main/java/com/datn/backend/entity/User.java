package com.datn.backend.entity;

import com.datn.backend.entity.enums.Role;
import com.datn.backend.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "display_name", length = 26)
    private String displayName;

    private LocalDate birthday;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
