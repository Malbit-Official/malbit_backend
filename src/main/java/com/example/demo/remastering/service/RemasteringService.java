package com.example.demo.remastering.service;

import com.example.demo.conversation.service.ConversationService;
import com.example.demo.entity.*;
import com.example.demo.log.repository.LogDetailRepository;
import com.example.demo.log.repository.LogRepository;
import com.example.demo.remastering.dto.AiServerResponseDto;
import com.example.demo.remastering.dto.MeetingAiServerResponseDto;
import com.example.demo.remastering.dto.MeetingAnalysisResponse;
import com.example.demo.remastering.dto.RemasteringLogResponse;
import com.example.demo.users.repository.UserRepository;
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

    private final WebClient webClient;                 // AI 서버와 통신용
    private final ConversationService conversationService; // DB 저장용
    private final UserService userService; // 통계 업데이용
    private final LogRepository logRepository;
    private final LogDetailRepository logDetailRepository;
    private final UserRepository userRepository;

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

                    // --- [추가] DB 저장 로직 시작 ---
                    // 1. 메인 Log 생성 및 저장
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

                    Log log = Log.builder()
                            .user(user)
                            .title(java.time.LocalDate.now().toString() + " 업무 분석")
                            .date(java.time.LocalDate.now())
                            .startTime(java.time.LocalTime.now())
                            .duration("분석 완료") // 실제 음성 길이 정보를 받으면 업데이트 가능
                            .type(LogType.CONFERENCE)
                            .build();

                    Log savedLog = logRepository.save(log);

                    // 2. LogDetail 저장 (요약, 결정사항, 할 일)
                    java.util.List<LogDetail> details = new java.util.ArrayList<>();

                    // 요약
                    if (data.getSummary() != null && !data.getSummary().isBlank()) {
                        details.add(LogDetail.builder()
                                .log(savedLog)
                                .content(data.getSummary())
                                .type(DetailType.SUMMARY)
                                .build());
                    }

                    // 결정사항 (checklists)
                    if (data.getChecklists() != null) {
                        for (String item : data.getChecklists()) {
                            details.add(LogDetail.builder()
                                    .log(savedLog)
                                    .content(item)
                                    .type(DetailType.DECISION)
                                    .build());
                        }
                    }

                    // 할 일 (schedules)
                    if (data.getSchedules() != null) {
                        for (MeetingAiServerResponseDto.ScheduleDto schedule : data.getSchedules()) {
                            details.add(LogDetail.builder()
                                    .log(savedLog)
                                    .content("[" + schedule.getCategory() + "] " + schedule.getTitle() + " (" + schedule.getTime() + ")")
                                    .type(DetailType.TODO)
                                    .assignee(schedule.getCategory())
                                    .build());
                        }
                    }

                    if (!details.isEmpty()) {
                        logDetailRepository.saveAll(details);
                    }
                    // --- [추가] DB 저장 로직 종료 ---

                    return new MeetingAnalysisResponse(
                            String.valueOf(savedLog.getId()),
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
