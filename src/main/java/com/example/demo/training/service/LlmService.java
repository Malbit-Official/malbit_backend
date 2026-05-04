package com.example.demo.training.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    /* AI 분석 원문을 Claude 3 Sonnet을 통해 정제하는 로직 */
    public String refineSentence(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            log.warn("[LLM Warning] 정제할 텍스트가 비어있습니다.");
            return "";
        }

        log.info("[LLM Request] 문장 정제 시작: {}", rawText);

        try {
            // TODO : AWS 계정 복구 후 Bedrock Client 호출 로직 추가 예정
            // 1. Bedrock Runtime Client 설정
            // 2. Claude 3용 프롬프트 생성 (반복 문구 제거, 자연스러운 문장 교정 등)
            // 3. 모델 호출 및 결과 파싱

            // 임시 로직
            String mockRefined = rawText.replaceAll("(.+)\\1+", "$1");

            log.info("[LLM Success] 문장 정제 완료");
            return mockRefined;

        } catch (Exception e) {
            log.error("[LLM Error] Claude 3 호출 중 예외 발생: {}", e.getMessage());
            return rawText;
        }
    }
}
