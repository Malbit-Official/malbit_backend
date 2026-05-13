package com.example.demo.suggestion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class AiSuggestionResponse {
    private String status;
    private SuggestionData data;

    @Getter
    @NoArgsConstructor
    public static class SuggestionData {
        private List<Recommendation> recommendations;
    }

    @Getter
    @NoArgsConstructor
    public static class Recommendation {
        private String speech;
        private String tip;
    }
}