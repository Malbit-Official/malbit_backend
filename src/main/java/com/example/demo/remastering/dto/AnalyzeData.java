package com.example.demo.remastering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalyzeData {

    // JSON에서는 "log_id" 이므로 @JsonProperty로 매핑
    @JsonProperty("log_id")
    private String logId;

    // JSON: "raw"
    private String raw;

    // JSON: "refined"
    private String refined;
}