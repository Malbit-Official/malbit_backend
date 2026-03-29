package com.example.demo.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStepRequest {

    private Long sessionId; // 현재 연습 세션 ID
    private Integer currentStep; // 현재 몇 번째 단계인지 (1,2,3...)
    private String userSpeech; // 앱에서 STT로 변환된 사용자의 발음 텍스트

}
