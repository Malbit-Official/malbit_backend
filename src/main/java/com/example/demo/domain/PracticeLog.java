package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "practice_log")
public class PracticeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long practiceId; // 연습ID (PK)

    // 누구의 연습 기록인지 연결 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 문장을 연습했는지 연결 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(nullable = false)
    private Integer accuracyScore; // 정확도 점수

    private String audioFileUrl; // 연습음성경로

    @Column(columnDefinition = "TEXT", nullable = false)
    private String wrongWords; // 부정확발음

    @Column(nullable = false, updatable = false)
    private LocalDateTime practicedAt; // 연습일시

    @PrePersist
    public void prePersist() {
        this.practicedAt = LocalDateTime.now();
    }

    @Builder
    public PracticeLog(User user, Template template, Integer accuracyScore,
                       String audioFileUrl, String wrongWords) {
        this.user = user;
        this.template = template;
        this.accuracyScore = accuracyScore;
        this.audioFileUrl = audioFileUrl;
        this.wrongWords = wrongWords;
    }
}