package com.example.demo.remastering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문장 리마스터링 응답")
public class RemasteringLogResponse {

    @Schema(description = "로그 ID", example = "50")
    @JsonProperty("log_id")
    private Long logId;

    @Schema(description = "원본 발화 텍스트", example = "결제... 해줄... 거야?")
    @JsonProperty("original_speech")
    private String originalSpeech;

    @Schema(description = "리마스터링된 텍스트", example = "결제 도와드릴까요?")
    @JsonProperty("refined_text")
    private String refinedText;

    @Schema(description = "응답 지연 시간(ms)", example = "850")
    @JsonProperty("latency_ms")
    private Integer latencyMs;

    public RemasteringLogResponse() {
    }

    public RemasteringLogResponse(Long logId, String originalSpeech, String refinedText, Integer latencyMs) {
        this.logId = logId;
        this.originalSpeech = originalSpeech;
        this.refinedText = refinedText;
        this.latencyMs = latencyMs;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getOriginalSpeech() {
        return originalSpeech;
    }

    public void setOriginalSpeech(String originalSpeech) {
        this.originalSpeech = originalSpeech;
    }

    public String getRefinedText() {
        return refinedText;
    }

    public void setRefinedText(String refinedText) {
        this.refinedText = refinedText;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }
}