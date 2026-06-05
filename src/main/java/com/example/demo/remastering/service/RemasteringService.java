package com.example.demo.remastering.service;

import com.example.demo.conversation.service.ConversationService;
import com.example.demo.entity.ConversationLog;
import com.example.demo.remastering.dto.AnalyzeApiResponse;
import com.example.demo.remastering.dto.AnalyzeData;
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

@Service
@RequiredArgsConstructor
public class RemasteringService {

    private final WebClient webClient;                 // AI 서버와 통신용
    private final ConversationService conversationService; // DB 저장용
    private final UserService userService;             // 통계 업데이트용

    /**
     * 문장 리마스터링 (/api/analyze 연동)
     */
    public Mono<RemasteringLogResponse> remaster(
            String email,            // 유저 이메일
            MultipartFile audioFile, // 음성 파일
            String preferredTone     // 선호 말투
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
            .uri("/api/analyze")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .retrieve()
            .bodyToMono(AnalyzeApiResponse.class)
            .map(res -> {
                long latency = System.currentTimeMillis() - startTime;

                if (!"SUCCESS".equalsIgnoreCase(res.getStatus())) {
                    throw new RuntimeException("AI 서버 응답 실패: " + res.getStatus());
                }

                AnalyzeData data = res.getData();
                String raw = data.getRaw();
                String refined = data.getRefined();

                if (raw == null || raw.isBlank()) {
                    raw = "인식된 내용 없음";
                }
                if (refined == null || refined.isBlank()) {
                    refined = raw;
                }

                userService.addCorrection(email, 50);

                ConversationLog savedLog = conversationService.saveResult(
                        email,
                        raw,
                        refined,
                        latency
                );

                return new RemasteringLogResponse(
                        savedLog.getLogId(),
                        savedLog.getSttOrigin(),
                        savedLog.getRefinedText(),
                        (int) latency
                );
            });
    }

    /**
     * 회의 분석 (/api/analyze-meeting 연동)
     */
    public Mono<MeetingAnalysisResponse> analyzeMeeting(String email, MultipartFile audioFile) {
        long startTime = System.currentTimeMillis();

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", audioFile.getResource());

        return webClient.post()
                .uri("/api/analyze-meeting")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(MeetingAiServerResponseDto.class)
                .map(aiRes -> {
                    long latency = System.currentTimeMillis() - startTime;

                    MeetingAiServerResponseDto.MeetingData data = aiRes.getData();

                    userService.increaseSummary(email);

                    ConversationLog savedLog = conversationService.saveResult(
                            email,
                            data.getRaw_text(),
                            data.getSummary(),
                            latency
                    );

                    return new MeetingAnalysisResponse(
                            String.valueOf(savedLog.getLogId()),
                            savedLog.getSttOrigin(),
                            savedLog.getRefinedText(),
                            data.getChecklists(),
                            data.getSchedules(),
                            (int) latency
                    );
                });
    }

}
