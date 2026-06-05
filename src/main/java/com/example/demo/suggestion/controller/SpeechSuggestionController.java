package com.example.demo.suggestion.controller;

import com.example.demo.global.common.ApiResponse;
import com.example.demo.suggestion.dto.AiSuggestionRequest;
import com.example.demo.suggestion.dto.AiSuggestionResponse;
import com.example.demo.suggestion.service.SpeechSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "상황별 발화 추천", description = "상황에 맞는 적절한 발화 문장을 추천합니다.")
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class SpeechSuggestionController {

    private final SpeechSuggestionService speechSuggestionService;

    /* 상황 발화 추천 API */
    @Operation(summary = "상황별 문장 추천")
    @PostMapping("/suggest")
    public Mono<ResponseEntity<ApiResponse<AiSuggestionResponse>>> suggestSpeech(
            @RequestBody AiSuggestionRequest request
    ) {
        return speechSuggestionService.getSuggestions(request)
                .map(result -> ResponseEntity.ok(ApiResponse.success("문장 추천 성공", result)));
    }
}
