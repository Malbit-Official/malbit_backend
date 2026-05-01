package com.example.demo.training.service;

import com.example.demo.entity.*;
import com.example.demo.training.dto.*;
import com.example.demo.training.repository.ScenarioStepRepository;
import com.example.demo.training.repository.StepResultRepository;
import com.example.demo.training.repository.TrainingCategoryRepository;
import com.example.demo.training.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingCategoryRepository categoryRepository;
    private final ScenarioStepRepository stepRepository;
    private final TrainingSessionRepository sessionRepository;
    private final StepResultRepository stepResultRepository;

    private final String uploadPath = System.getProperty("user.dir") + "/uploads/";
    private final WebClient aiWebClient;

    /* 특정 직무 연습 시작 로직 */
    public TrainingStartResponse startTraining(TrainingStartRequest request, User user) {

        // 카테고리 존재 여부 확인
        TrainingCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // 해당 카테고리의 첫 번째 단계(stepOrder = 1) 조회
        ScenarioStep firstStep = stepRepository.findByCategory_IdAndStepOrder(category.getId(), 1)
                .orElseThrow(() -> new IllegalStateException("해당 카테고리에 설정된 시나리오 단계가 없습니다."));

        // 새로운 연습 세션 생성 (상태 저장)
        TrainingSession session = TrainingSession.builder()
                .user(user)
                .category(category)
                .currentStep(firstStep)
                .build();

        TrainingSession savedSession = sessionRepository.save(session);

        // DTO로 변환하여 반환 (세션 ID와 첫 번째 스텝 정보 포함)
        return TrainingStartResponse.from(savedSession.getId(), firstStep);

    }

    /* 발음 분석 및 다음 단계 진행 로직 */
    public TrainingStepResponse processStep(TrainingStepRequest request, User user) {

        // 현재 세션 및 유저 검증
        TrainingSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 세션입니다."));

        if(!session.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("본인의 연습 세션만 진행할 수 있습니다.");
        }

        // 현재 단계의 정담 (hinText 또는 별도 정답필드)과 사용자 발음 비교
        ScenarioStep currentStep = session.getCurrentStep();
        int score = calculateSimilarity(currentStep.getHintText(), request.getUserSpeech());

        // 이번 단계의 결과를 DB에 저장 (나중에 결과창에서 쓰임)
        StepResult stepResult = StepResult.builder()
                .session(session)
                .step(currentStep)
                .score(score)
                .isPassed(score >= 70) // 70점 이상이면 성공으로 간주
                .build();
        stepResultRepository.save(stepResult);

        // 다음 단계(currentStepOrder + 1) 조회
        int nextOrder = currentStep.getStepOrder() + 1;
        Optional<ScenarioStep> nextStepOpt = stepRepository.findByCategory_IdAndStepOrder(
                session.getCategory().getId(), nextOrder
        );

        // 세션 상태 업데이트 (다음 단계가 있다면 업데이트)
        nextStepOpt.ifPresent(session::updateStep);

        // 응답 생성 (분석 결과 + 다음 단계 정보)
        return TrainingStepResponse.builder()
                .score(score)
                .feedback(generateFeedback(score))
                .retryScript(currentStep.getRetryScript())
                // 다음 단계 정보 매핑
                .nextStepOrder(nextStepOpt.map(ScenarioStep::getStepOrder).orElse(null))
                .nextSituation(nextStepOpt.map(ScenarioStep::getCurrentSituation).orElse("연습 종료"))
                .nextGuestQuestion(nextStepOpt.map(ScenarioStep::getGuestScript).orElse(null))
                .nextHintText(nextStepOpt.map(ScenarioStep::getHintText).orElse(null))
                .nextMissionText(nextStepOpt.map(ScenarioStep::getMissionText).orElse(null))
                .isLast(nextStepOpt.isEmpty())
                .build();
    }


    /* 연습 종료 및 결과 저장 로직 */
    public TrainingResultResponse finishTraining(Long sessionId, User user) {

        // 세션 조회 및 검증
        TrainingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 세션입니다."));

        // 세션 상태 업데이트 (종료 처리)
        session.completeSession();

        // [실제 데이터 조회] 이 세션에서 진행한 모든 StepResult 가져오기
        List<StepResult> results = stepResultRepository.findAllBySession(session);

        // [자동 문구 생성] 성공한 단계의 '미리 정해진 문구'들만 리스트로 만듦
        List<String> checks = results.stream()
                .filter(StepResult::isPassed) // 성공한 단계만 필터링
                .map(res -> res.getStep().getSuccessMessage()) // ScenarioStep에 successMessage 필드가 있어야 함
                .collect(Collectors.toList());

        // 평균 점수 계산
        double avgScore = results.stream()
                .mapToInt(StepResult::getScore)
                .average().orElse(0.0);

        return TrainingResultResponse.builder()
                .sessionId(session.getId())
                .feedbackChecklist(checks) // DB 데이터 기반 자동 생성
                .evaluation(generateTotalEvaluation(avgScore)) // 점수 기반 총평 생성
                .nextCategoryId(session.getCategory().getId() + 1)
                .build();

    }

    /* 음성 파일 로컬에 저장 로직 */
    public File processVoice(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 음성 파일이 없습니다.");
        }

        try {
            // 저장할 uploads 폴더가 없으면 생성
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 고유한 파일명 UUID 생성 (파일 이름 중복 방지)
            String originalFileName = file.getOriginalFilename();
            String storeFileName = UUID.randomUUID() + "_" + originalFileName;

            File targetFile = new File(folder, storeFileName);
            Path targetPath = targetFile.toPath();

            // 파일 저장
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", targetPath);

            return targetFile;
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("음성 파일 저장 실패", e);
        }
    }

    /* 저장된 음성 파일명을 전달하여 AI 분석 결과(JSON)를 받아오는 로직 */
    public String getAiAnalysis(File savedFile) {
        log.info("AI 서버 분석 요청 시작: {}", savedFile.getName());
        try {
            FileSystemResource resource = new FileSystemResource(savedFile);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", resource);

            MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

            return aiWebClient.post()
                    .uri("/analyze")
                    .bodyValue(multipartBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    //.timeout(Duration.ofMinutes(3))
                    .block();

        } catch (Exception e) {
            log.error("AI 서버 통신 중 에러 발생: {}", e.getMessage());
            return "AI 분석 실패: " + e.getMessage();
        }
    }

    /* 발음 유사도 계산 알고리즘 (임시 구현) */
    // 추후 확장 예정
    private int calculateSimilarity(String answer, String userSpeech) {
        if (answer == null || userSpeech == null) return 0;
        // 단순 텍스트 일치율 예시
        if (answer.trim().equals(userSpeech.trim())) return 100;
        return 75; // 테스트용 고정값
    }

    private String generateFeedback(int score) {
        if (score >= 90)
            return "완벽한 발음입니다! 자신감 있게 말씀하셨네요.";
        if (score >= 70)
            return "잘 들립니다. 조금만 더 천천히 말씀해보세요.";
        return "발음이 조금 불분명해요. 다시 한 번 시도해볼까요?";
    }

    private String generateTotalEvaluation(double avgScore) {
        if (avgScore >= 90) return "발음이 매우 정확하고 자연스럽습니다! 완벽해요.";
        if (avgScore >= 70) return "전반적으로 훌륭합니다. 몇몇 단어만 더 명확하게 연습해보세요.";
        return "조금 더 천천히, 명 또박또박 말하는 연습을 하면 더 좋아질 거예요!";
    }
}
