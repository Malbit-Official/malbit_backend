// 통계 데이터 조회 (수정/입력용)
package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsRequest {
    private Integer totalCorrectionCount; // 총 보정 횟수
    private Integer averageCorrectionIntensity; // 평균 보정 강도
    private Integer completedRoleplays; // 완료한 상황극
    private Integer generatedSummaries; // 생성한 요약
}
