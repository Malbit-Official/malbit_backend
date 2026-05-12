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
    public String[] refineSentence(String rawInput) {

        try {
            Map<String, Object> fullResponse = webClient.post()
                    .uri("/api/analyze")
                    .bodyValue(Map.of("text", rawInput))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("[LLM Raw Response] AI 서버 응답 전체: {}", fullResponse);

            if (fullResponse != null && "SUCCESS".equals(fullResponse.get("status"))) {
                Map<String, Object> data = (Map<String, Object>) fullResponse.get("data");

                if (data != null) {
                    String refined = (String) data.get("refined_text");
                    String raw = (String) data.get("raw_text");

                    if (refined == null || refined.isBlank()) refined = rawInput;
                    if (raw == null || raw.isBlank()) raw = rawInput;

                    return new String[]{refined, raw};
                }
            }
        }
            catch (Exception e) {
            log.error("[LLM Error] 문장 정제 중 오류 발생: {}", e.getMessage());
        }
        return new String[]{rawInput, rawInput};
    }
}
