package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_statistics")
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer totalCorrectionCount = 0;      // 총 보정 횟수
    private Integer averageCorrectionIntensity = 0; // 평균 보정 강도
    private Integer completedRoleplays = 0;         // 완료한 상황극
    private Integer generatedSummaries = 0;         // 생성한 요약

    // 통계 업데이트 메서드
    public void updateStats(int correction, int intensity, int roleplay, int summary) {
        this.totalCorrectionCount += correction;
        this.averageCorrectionIntensity = intensity; // 강도는 보통 평균값으로 갱신
        this.completedRoleplays += roleplay;
        this.generatedSummaries += summary;
    }
}