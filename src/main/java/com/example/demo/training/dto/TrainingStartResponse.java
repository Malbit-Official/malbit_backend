// 연습이 시작되면 프론트에 세션ID와 첫 번쨰 단계의 보든 정보를 전송
package com.example.demo.training.dto;

import com.example.demo.entity.ScenarioStep;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrainingStartResponse {

    private Long sessionId; // 생성된 세션 ID
    private Integer stepOrder; // 1 (첫 번째 단계)
    private String currentSituation; // "손님이 음료를 주문하려고 합니다."
    private String guestScript; // "아이스 아메리카노 한 잔 주세요."
    private String hintText; // "네, 아이스 아메리카노 한 잔 맞으시죠?"
    private String missionText; // "손님 주문에 응대해 보세요!"

    // 엔티티를 DTO로 변환하는 편리한 메소드
    public static TrainingStartResponse from(Long sessionId, ScenarioStep step) {
        return TrainingStartResponse.builder()
                .sessionId(sessionId)
                .stepOrder(step.getStepOrder())
                .currentSituation(step.getCurrentSituation())
                .guestScript(step.getGuestScript())
                .hintText(step.getHintText())
                .missionText(step.getMissionText())
                .build();
    }
}
