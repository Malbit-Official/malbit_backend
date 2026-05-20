// 일정 상세 수정
package com.example.demo.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskUpdateRequest {

    private String content; // 수정할 내용

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start_at; // 시작 시간 수정

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end_at; // 마감 시간 수정

    private String category; // 카테고리 수정
}
