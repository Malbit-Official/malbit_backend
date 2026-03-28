// 연습 세션 상태 (사용자가 연습을 시작했을 때,
// "누가, 어떤 카테고리를, 현재 몇 단계까지 했는지" 실시간으로 기록하는 장부)
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "training_session")
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 연습 중인 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TrainingCategory category; // 선택한 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id")
    private ScenarioStep currentStep; // 현재 진행 중인 단계

    private LocalDateTime startedAt;

    @Builder
    public TrainingSession(User user, TrainingCategory category, ScenarioStep currentStep) {
        this.user = user;
        this.category = category;
        this.currentStep = currentStep;
        this.startedAt = LocalDateTime.now();
    }

    // 다음 단계로 업데이트하는 메소드
    public void updateStep(ScenarioStep nextStep) {
        this.currentStep = nextStep;
    }

}
