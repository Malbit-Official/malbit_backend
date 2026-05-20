// 다가오는 일정 알림 조회
package com.example.demo.calendar.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpcomingTasksResponse {

    private List<UpcomingTaskDto> upcoming_tasks;

    @Getter
    @Builder
    public static class UpcomingTaskDto {

        private Long task_id;
        private String content;
        private String remaining_time; // 예: "D-2", "3시간 전", "15분 전"
        private Long d_day; // 예: 2 (D-Day 숫자만 추출)
        private String category;
    }
}
