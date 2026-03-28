package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId; // 할일ID (PK)

    // 어떤 대화 로그이세ㅓ 파생되었는지 연결 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private ConversationLog log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSession session;

    @Column(nullable = false)
    private String content; // 할일내용

    private LocalDateTime startAt; // 업무 시작일

    private LocalDateTime endAt; // 업무 마감일

    @Column(nullable = false)
    private Boolean isCompleted = false; // 완료여부

    private String category; // 업무 카테고리

    @Builder
    public Task(ConversationLog log, ConversationSession session, String content,
                LocalDateTime startAt, LocalDateTime endAt, Boolean isCompleted, String category) {
        this.log = log;
        this.session = session;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isCompleted = (isCompleted != null) ? isCompleted : false;
        this.category = category;
    }

}