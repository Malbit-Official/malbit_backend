package com.example.demo.suggestion.service;

import com.example.demo.suggestion.dto.AiSuggestionRequest;
import com.example.demo.suggestion.dto.AiSuggestionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SpeechSuggestionService {

    private final WebClient webClient;

    /* 상황 발화 추천 로직 */
    public SpeechSuggestionService(WebClient.Builder webClientBuilder,
                                   @Value("${ai.server.url}") String aiServerUrl) {
        this.webClient = webClientBuilder.baseUrl(aiServerUrl).build();
    }

    public Mono<AiSuggestionResponse> getSuggestions(AiSuggestionRequest request) {
        return webClient.post()
                .uri("/api/suggest-speech")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiSuggestionResponse.class);
    }
}
