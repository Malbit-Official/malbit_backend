// 사용자의 현재 연습 진행 상태를 저장하고 조회할 때 사용
package com.example.demo.training.repository;

import com.example.demo.entity.TrainingSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    // 특정 사용자가 현재 진행 중인 세션이 있는지 확인
    Optional<TrainingSession> findByUserAndCategoryId(User user, Long categoryId);
}
