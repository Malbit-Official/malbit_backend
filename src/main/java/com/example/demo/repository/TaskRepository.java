package com.example.demo.repository;

import com.example.demo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 특정 대화 세션에서 추출된 할 일들만 모아보고 싶을 때
    @Query("select t from Task t where t.session.sessionId = :sessionId")
    List<Task> findBySessionId(@Param("sessionId") Long sessionId);

    // 아직 완료되지 않은(is_completed = false) 할 일만 가져오고 싶을 때
    List<Task> findByIsCompletedFalse();
}
