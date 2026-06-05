package com.example.demo.log.dto;

import com.example.demo.entity.Log;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class LogResponseDto {

    private Long logId;
    private String title;
    private String time;
    private String duration;
    private String type;

    public static LogResponseDto from(Log log) {
        return LogResponseDto.builder()
                .logId(log.getId())
                .title(log.getTitle())
                .time(log.getStartTime().toString())
                .duration(log.getDuration())
                .type(log.getType().name())
                .build();
    }
}
