package com.example.demo.suggestion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AiSuggestionRequest {
    private String category;
    @JsonProperty("user_input")
    private String userInput;
}
