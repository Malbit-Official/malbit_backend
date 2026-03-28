// 세부 상황 단계
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "scenario_step")
public class ScenarioStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TrainingCategory category;

    private Integer stepOrder; // 연습 순서

    private String currentSituation; // 현재 상황 (예: "#주문 받기", "#기본 응대")
    private String guestScript; // 손님 대사 (예: "아이스아메리카노, 한 잔 주세요"
    private String hintText; // 힌트보기
    private String missionText; // 미션 안내 문구 (예: "손님 주문에 응대해보세요!")
    private String retryScript; // 발음 부정확 시 AI가 다시 물어볼 대사 (예: "잘 못 들었어요. 다시 말씀해 주시겠어요?")

    @Builder
    public ScenarioStep(TrainingCategory category, Integer stepOrder, String currentSituation,
                        String guestScript, String hintText, String missionText, String retryScript) {
        this.category = category;
        this.stepOrder = stepOrder;
        this.currentSituation = currentSituation;
        this.guestScript = guestScript;
        this.hintText = hintText;
        this.missionText = missionText;
        this.retryScript = retryScript;
    }

}
