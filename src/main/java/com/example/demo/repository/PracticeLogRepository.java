package com.example.demo.repository;

import com.example.demo.entity.PracticeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PracticeLogRepository extends JpaRepository<PracticeLog, Long> {

    // 특정 사용자의 발음 연습 기록을 최신순으로 가져올 때 사용
    @Query("select p from PracticeLog p where p.user.userId = :userId order by p.practicedAt desc")
    List<PracticeLog> findByUserId(@Param("userId") Long userId);
}
