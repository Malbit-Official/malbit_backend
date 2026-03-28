package com.example.demo.repository;

import com.example.demo.entity.ConversationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {

    // 특정 대화 세션의 모든 로그를 시간순으로 가져올 때
    @Query("select c from ConversationLog c where c.session.sessionId = :sessionId order by c.createdAt asc")
    List<ConversationLog> findBySessionId(@Param("sessionId") Long sessionId);
}
