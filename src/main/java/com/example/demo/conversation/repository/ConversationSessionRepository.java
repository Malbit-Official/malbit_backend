package com.example.demo.conversation.repository;

import com.example.demo.entity.ConversationSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    // 특정 유저의 현재 활성화된 세션을 찾는 메서드
    Optional<ConversationSession> findByUserAndIsActiveTrue(User user);
}
