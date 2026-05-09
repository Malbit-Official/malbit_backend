package com.example.demo.remastering.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MeetingAnalysisResponse {
    private String meetingId;
    private String rawText;
    private String summary;
    private List<String> checklists;
    private List<MeetingAiServerResponseDto.ScheduleDto> schedules;
    private int latency;
}