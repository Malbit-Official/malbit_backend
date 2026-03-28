package com.example.demo.training.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainingStepResponse {

    // 이번 단계 분석 결과
    private Integer score; // 발음 유사도 점수 (0~100)
    private String feedback; // AI 피드백 ("잘하셨어요!" 등의 간단한 피드백)
    private String retryScript; // "다시 말씁해 주시겠어요?"

    // 다음 단계 정보
    private Integer nextStepOrder;
    private String nextSituation;
    private String nextGuestQuestion;
    private String nextHintText;
    private String nextMissionText;
    private String nextGuide;
    private boolean isLast; // 마지막 단계면 true
}
