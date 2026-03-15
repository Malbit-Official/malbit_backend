package com.example.demo.repository;

import com.example.demo.domain.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    @Query("select s from ConversationSession s where s.user.userId = :userId")
    List<ConversationSession> findByUserId(@Param("userId") Long userId);
}
