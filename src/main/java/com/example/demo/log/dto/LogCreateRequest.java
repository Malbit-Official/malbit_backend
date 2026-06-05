package com.example.demo.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogCreateRequest {

    private String title; // 사용자가 입력하거나 자동 생성된 제목
    private String rawContent; // AI가 요약할 전체 대화 내용 (STT 결과물 등)
    private LocalDateTime startTime; // 회의 시작 시간
    private String duration; // 소요 시간 (예: "15:29")
}
