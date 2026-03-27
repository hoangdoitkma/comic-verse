package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "new_chapter", nullable = false)
    @Builder.Default
    private Boolean newChapter = true;

    @Column(name = "comment_reply", nullable = false)
    @Builder.Default
    private Boolean commentReply = true;

    @Column(name = "system_notice", nullable = false)
    @Builder.Default
    private Boolean systemNotice = true;
}
