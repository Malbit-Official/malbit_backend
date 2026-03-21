package com.example.demo.repository;

import com.example.demo.domain.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findByUserEmail(String email);
}
