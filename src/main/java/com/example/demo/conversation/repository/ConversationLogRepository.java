package com.example.demo.conversation.repository;

import com.example.demo.entity.ConversationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {
}
