package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class StepResult {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private TrainingSession session; // 어떤 연습 세션에 속하는지

    @ManyToOne(fetch = FetchType.LAZY)
    private ScenarioStep step; // 어떤 단계(상황)였는지

    private Integer score; // 해당 단계의 발음 점수
    private boolean isPassed; // 기준 점수(예: 70점)를 넘었는지 여부

    @Builder
    public StepResult(TrainingSession session, ScenarioStep step, Integer score, boolean isPassed) {
        this.session = session;
        this.step = step;
        this.score = score;
        this.isPassed = isPassed;
    }
}
