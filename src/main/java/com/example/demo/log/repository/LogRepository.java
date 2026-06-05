package com.example.demo.log.repository;

import com.example.demo.entity.Log;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    // 특정 유저의 특정 날짜 기록을 시작 시간 내림차순으로 조회
    List<Log> findByUserAndDateOrderByStartTimeDesc(User user, LocalDate date);
}
