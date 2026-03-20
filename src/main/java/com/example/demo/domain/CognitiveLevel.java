package com.example.demo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CognitiveLevel {
    LEVEL_1(1, "1단계: 매우 낮음 (단어 위주 소통)"),
    LEVEL_2(2, "2단계: 낮음 (간단한 문장 이해"),
    LEVEL_3(3, "3단계: 보통 (일상 대화 가능)"),
    LEVEL_4(4, "4단계: 높음 (추상/비유 이해)"),
    LEVEL_5(5, "5단계: 매우 높음 (정교한 소통)");

    private final int level;
    private final String description;
}
