package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "conversation_log")
public class ConversationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sttOrigin;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String refinedText;

    private Long latencyMs;

    @Column(nullable = false)
    private Boolean isArchived = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.isArchived == null) this.isArchived = false;
    }

    @Builder
    public ConversationLog(ConversationSession session, String sttOrigin, String refinedText, Long latencyMs, Boolean isArchived) {
        this.session = session;
        this.sttOrigin = sttOrigin;
        this.refinedText = refinedText;
        this.latencyMs = latencyMs;
        this.isArchived = (isArchived != null) ? isArchived : false;
    }
}