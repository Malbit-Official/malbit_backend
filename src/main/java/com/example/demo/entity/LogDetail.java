// 업무 기록 상세 조회
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lod_id")
    private Log log;

    private String content; // 내용 (예: "메인 페이지 UI 완료")

    @Enumerated(EnumType.STRING)
    private DetailType type; // SUMMARY, DECISION, TODO 구분

    private String assignee; // TODO일 경우에만 사용 (예: "참여자 2")
}
