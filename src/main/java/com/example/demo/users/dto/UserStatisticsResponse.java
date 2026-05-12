// 통계 데이터 조회 (조회용)
package com.example.demo.users.dto;

import com.example.demo.entity.UserStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserStatisticsResponse {
    private Integer totalCorrectionCount; // 총 보정 횟수
    private Integer averageCorrectionIntensity; // 평균 보정 강도
    private Integer completedRoleplays; // 완료한 상황극
    private Integer generatedSummaries; // 생성한 요약

    public static UserStatisticsResponse from(UserStatistics stats) {
        return UserStatisticsResponse.builder()
                .totalCorrectionCount(stats.getTotalCorrectionCount())
                .averageCorrectionIntensity(stats.getAverageCorrectionIntensity())
                .completedRoleplays(stats.getCompletedRoleplays())
                .generatedSummaries(stats.getGeneratedSummaries())
                .build();
    }
}
