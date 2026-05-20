package com.example.demo.training.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StepReviewDto {

    private Integer stepOrder; // 몇 번째 단계였는지
    private String situation; // 상황 설명 (예: "주문 받기")
    private String rawText; // 사용자 발음 원문 (AI 분석 결과)
    private String refinedText; // LLM이 정제한 문장
    private Integer score; // 발음 점수
    private boolean isPassed; // 통과 여부
}
