package com.example.demo.training.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TrainingResultResponse {

    private Long sessionId;
    private List<String> feedbackChecklist; // ["주문 응대 성공", "옵션 확인 자연스러움", "결제 안내 적절함"]
    private String evaluation; // 종합 평가 메시지
    private Long nextCategoryId;
}
