package com.example.demo.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LogDetailResponseDto {

    private String title;
    private String date;
    private List<String> summaries; // 요약 리스트
    private List<String> decisions; // 결정 사항 리스트
    private List<TodoDto> todos; // 할 일 리스트 (담당자 포함)

    @Getter
    @AllArgsConstructor
    public static class TodoDto {
        private String assignee;
        private String content;
    }
}
