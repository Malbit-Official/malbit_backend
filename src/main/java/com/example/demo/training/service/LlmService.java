package com.example.demo.training.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final WebClient webClient;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /* 문장 정제 요청 로직 */
    // 파이썬 AI 서버 혹은 AWS Bedrock(Claude 3)과 통신하여 사용자의 발화를 정제된 문장으로 변합니다.
    public String refineSentence(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        try {
            log.info("[LLM Request] 문장 정제 요청 시작: {}", rawText);

            // 비동기 통신을 동기적으로 처리
            Map<String, Object> response = webClient.post()
                    .uri("/analyze")
                    .bodyValue(Map.of("text", rawText))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("refined_text");
            }

            return rawText;

        } catch (Exception e) {
            log.error("[LLM Error] 문장 정제 중 오류 발생: {}", e.getMessage());
            return rawText;
        }
    }
}
