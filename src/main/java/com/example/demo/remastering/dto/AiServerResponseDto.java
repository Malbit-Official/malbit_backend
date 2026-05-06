package com.example.demo.remastering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiServerResponseDto {

    @JsonProperty("raw_text")
    private String rawText;

    @JsonProperty("refined_text")
    private String refinedText;

    private String status;
}
