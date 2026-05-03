// 월간/주간 일정 조회
package com.example.demo.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarListResponse {

    private String date;
    private List<TaskSimpleDto> schedules;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskSimpleDto {

        private Long task_id;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime start_at;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime end_at;

        private String category;
    }
}
