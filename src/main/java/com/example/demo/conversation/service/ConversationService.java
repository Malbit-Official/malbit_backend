package com.example.demo.conversation.service;

import com.example.demo.conversation.repository.ConversationLogRepository;
import com.example.demo.conversation.repository.ConversationSessionRepository;
import com.example.demo.entity.ConversationLog;
import com.example.demo.entity.ConversationSession;
import com.example.demo.entity.User;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationLogRepository logRepository;
    private final ConversationSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /* AI 분석 결과 DB 저장 로직 */
    @Transactional
    public ConversationLog saveResult(String email, String raw, String refined, Long latency) {

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 현재 활성화된 대화 세션 조회 (없으면 새로 생성)
        ConversationSession session = sessionRepository.findByUserAndIsActiveTrue(user)
                .orElseGet(() -> sessionRepository.save(
                        ConversationSession.builder()
                                .user(user)
                                .title("실시간 대화 " + LocalDate.now())
                                .isActive(true)
                                .build()
                ));

        // 대화 로그 엔티티 생성 및 저장
        ConversationLog log = ConversationLog.builder()
                .session(session)
                .sttOrigin(raw)
                .refinedText(refined)
                .latencyMs(latency)
                .isArchived(false)
                .build();

        return logRepository.save(log);
    }
}
