package com.example.demo.log.repository;

import com.example.demo.entity.Log;
import com.example.demo.entity.LogDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogDetailRepository extends JpaRepository<LogDetail, Long> {

    List<LogDetail> findByLog(Log log); // 서비스에서 사용하는 메서드
}
