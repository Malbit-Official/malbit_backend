package com.example.demo.training.repository;

import com.example.demo.entity.StepResult;
import com.example.demo.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StepResultRepository extends JpaRepository<StepResult, Long> {

    // 세션별 결과를 모두 가져오는 메서드
    List<StepResult> findAllBySession(TrainingSession session);
}
