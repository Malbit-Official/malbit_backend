package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "conversation_session")
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Notnull (FK)
    private User user;

    private String title; // Nullable(기본값)

    @Column(columnDefinition = "TEXT")
    private String totalSummary; // Nullable (기본값)

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @Builder
    public ConversationSession(User user, String title, String totalSummary, Boolean isActive) {
        this.user = user;
        this.title = title;
        this.totalSummary = totalSummary;
        this.isActive = (isActive != null) ? isActive : true;
    }
}
