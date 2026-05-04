package com.example.demo.remastering.service;

import com.example.demo.conversation.service.ConversationService;
import com.example.demo.entity.ConversationLog;
import com.example.demo.remastering.dto.AiServerResponseDto;
import com.example.demo.remastering.dto.RemasteringLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RemasteringService {

    private final WebClient webClient; // AI 서버와 통신용
    private final ConversationService conversationService; // DB 저장용

    public Mono<RemasteringLogResponse> remaster(
            String email, // 세션 ID 대신 유저 이메일로 처리
            MultipartFile audioFile,
            String preferredTone
    ) {
        long startTime = System.currentTimeMillis();

        // AI 서버(FastAPI)로 보낼 멀티파트 데이터 구성
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", audioFile.getResource());

        // 말투 설정
        if (preferredTone != null && !preferredTone.isBlank()) {
            bodyBuilder.part("preferred_tone", preferredTone);
        }

        // AI 서버 호출 및 응답 처리
        return webClient.post()
                .uri("/analyze") // FastAPI의 엔드포인트
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(AiServerResponseDto.class)
                .map(aiRes -> {
                    long latency = System.currentTimeMillis() - startTime;

                    // ConversationService를 통해 DB에 저장
                    ConversationLog savedLog = conversationService.saveResult(
                            email,
                            aiRes.getRawText(),
                            aiRes.getRefinedText(),
                            latency
                    );

                    // 응답 생성
                    return new RemasteringLogResponse(
                            savedLog.getLogId(),
                            savedLog.getSttOrigin(),
                            savedLog.getRefinedText(),
                            (int) latency
                    );
                });
    }
}


