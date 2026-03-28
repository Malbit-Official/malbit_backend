package com.example.demo.training.service;

import com.example.demo.entity.ScenarioStep;
import com.example.demo.entity.TrainingCategory;
import com.example.demo.entity.TrainingSession;
import com.example.demo.entity.User;
import com.example.demo.training.dto.TrainingStartRequest;
import com.example.demo.training.dto.TrainingStartResponse;
import com.example.demo.training.dto.TrainingStepRequest;
import com.example.demo.training.dto.TrainingStepResponse;
import com.example.demo.training.repository.ScenarioStepRepository;
import com.example.demo.training.repository.TrainingCategoryRepository;
import com.example.demo.training.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingCategoryRepository categoryRepository;
    private final ScenarioStepRepository stepRepository;
    private final TrainingSessionRepository sessionRepository;

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
}
