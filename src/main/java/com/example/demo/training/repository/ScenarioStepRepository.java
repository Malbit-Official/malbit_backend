// 특정 카테고리의 특정 순서(1번, 2번...) 단계를 찾을 때 핵심 역할
package com.example.demo.training.repository;

import com.example.demo.entity.ScenarioStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScenarioStepRepository extends JpaRepository<ScenarioStep, Long> {

    // 특정 카테고리 ID와 단계 순서(1, 2, 3...)로 해당 스텝을 찾음
    Optional<ScenarioStep> findByCategory_IdAndStepOrder(Long categoryId, Integer stepOrder);
}
