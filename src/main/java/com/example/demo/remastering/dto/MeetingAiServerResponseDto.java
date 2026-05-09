package com.example.demo.remastering.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class MeetingAiServerResponseDto {
    private String status;
    private MeetingData data;

    @Getter
    @NoArgsConstructor
    public static class MeetingData {
        private String meeting_id;
        private String raw_text;
        private String summary;
        private List<String> checklists;
        private List<ScheduleDto> schedules;
    }

    @Getter
    @NoArgsConstructor
    public static class ScheduleDto {
        private String title;
        private String category;
        private String date;
        private String time;
        private String importance;
    }
}