package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "template")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId; // 템플릿ID (PK)

    // 어떤 상황에 속한 템플릿인지 연결 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_id", nullable = false)
    private Situation situation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawPhrase; // 기본문장

    @Column(columnDefinition = "TEXT", nullable = false)
    private String remasteredPhrase; // 정중한문장

    @Column(nullable = false)
    private Integer priorityScore = 0; // 추천순위

    @Builder
    public Template(Situation situation, String rawPhrase, String remasteredPhrase, Integer priorityScore) {
        this.situation = situation;
        this.rawPhrase = rawPhrase;
        this.remasteredPhrase = remasteredPhrase;
        this.priorityScore = (priorityScore != null) ? priorityScore : 0;
    }
}