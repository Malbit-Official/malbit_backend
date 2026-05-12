package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId; // 할일ID (PK)

    // 어떤 유저의 일정인지 직접 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 대화 로그에서 파생되었는지 연결 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = true)
    private ConversationLog log;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = true)
    private ConversationSession session;

    @Column(nullable = false)
    private String content; // 할일내용

    private LocalDateTime startAt; // 업무 시작일

    private LocalDateTime endAt; // 업무 마감일

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false; // 완료여부

    private String category; // 업무 카테고리

    public Task(User user, ConversationLog log, ConversationSession session, String content,
                LocalDateTime startAt, LocalDateTime endAt, Boolean isCompleted, String category) {
        this.user = user;
        this.log = log;
        this.session = session;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.completed = (isCompleted != null) ? isCompleted : false;
        this.category = category;

    }

    // 일정 수정을 위한 비즈니스 메서드
    public void update(String content, String category, LocalDateTime startAt, LocalDateTime endAt) {
        if (content != null) this.content = content;
        if (category != null) this.category = category;
        if (startAt != null) this.startAt = startAt;
        if (endAt != null) this.endAt = endAt;
    }

    // 체크박스 상태 반전 (토글)
    public void toggleCompletion() {
        this.completed = !this.completed;
    }

    // 체크박스 상태를 명시적으로 업데이트
    public void updateCompletion(boolean completed) {
        this.completed = completed;
    }

}