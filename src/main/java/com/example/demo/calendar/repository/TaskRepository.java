package com.example.demo.calendar.repository;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 특정 유저의 특정 기간 내 일정을 조회 (월간/주간 조회용)
    List<Task> findAllByUserAndStartAtBetween(User user, LocalDateTime start, LocalDateTime end);

    // 마감이 임박한 일정 조회 (다가오는 일정용)
    List<Task> findAllByUserAndStartAtAfterOrderByStartAtAsc(User user, LocalDateTime now);
}
