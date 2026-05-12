package com.example.demo.remastering.service;

import com.example.demo.conversation.service.ConversationService;
import com.example.demo.entity.ConversationLog;
import com.example.demo.remastering.dto.AiServerResponseDto;
import com.example.demo.remastering.dto.MeetingAiServerResponseDto;
import com.example.demo.remastering.dto.MeetingAnalysisResponse;
import com.example.demo.remastering.dto.RemasteringLogResponse;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RemasteringService {

    private final WebClient webClient; // AI 서버와 통신용
    private final ConversationService conversationService; // DB 저장용
    private final UserService userService; // 통계 업데이용

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
                .uri("/api/analyze") // FastAPI의 엔드포인트
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(AiServerResponseDto.class)
                .map(aiRes -> {
                    long latency = System.currentTimeMillis() - startTime;

                    // 데이터 유실 방지 및 기본값 설정
                    String raw = aiRes.getRawText();
                    String refined = aiRes.getRefinedText();

                    if (raw == null || raw.isBlank()) {
                        raw = "인식된 내용 없음";
                    }
                    if (refined == null || refined.isBlank()) {
                        refined = raw;
                    }

                    // 통계 자동 업데이트 추가 - 문장 보정 횟수 및 강도 증가
                    userService.addCorrection(email, 50);

                    // ConversationService를 통해 DB에 저장
                    ConversationLog savedLog = conversationService.saveResult(
                            email,
                            raw,
                            refined,
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


    public Mono<MeetingAnalysisResponse> analyzeMeeting(String email, MultipartFile audioFile) {
        long startTime = System.currentTimeMillis();

        // AI 서버(FastAPI)로 보낼 멀티파트 데이터 구성
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", audioFile.getResource());

        // AI 서버 호출 및 응답 처리
        return webClient.post()
                .uri("/api/analyze-meeting")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(MeetingAiServerResponseDto.class)
                .map(aiRes -> {
                    long latency = System.currentTimeMillis() - startTime;
                    MeetingAiServerResponseDto.MeetingData data = aiRes.getData();

                    // 통계 자동 업데이트 추가 - 회의 요약 횟수 증가
                    userService.increaseSummary(email);

                    return new MeetingAnalysisResponse(
                            data.getMeeting_id(),
                            data.getRaw_text(),
                            data.getSummary(),
                            data.getChecklists(),
                            data.getSchedules(),
                            (int) latency
                    );
                });
    }


}


