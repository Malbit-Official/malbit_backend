package com.example.demo.training.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiAnalysisResponse {

    private String status;

    @JsonProperty("raw_text") // 파이썬의 raw_text와 매칭
    private String rawText;

    @JsonProperty("refined_text") // 파이썬의 refined_text와 매칭
    private String refinedText;
}
