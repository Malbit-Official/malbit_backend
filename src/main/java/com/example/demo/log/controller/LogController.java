package com.example.demo.log.controller;

import com.example.demo.entity.User;
import com.example.demo.log.dto.LogDetailResponseDto;
import com.example.demo.log.dto.LogResponseDto;
import com.example.demo.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /* 업무 기록 목록 조회 API */
    @GetMapping
    public ResponseEntity<List<LogResponseDto>> getDailyLogs(

            @AuthenticationPrincipal User user, // 로그인된 유저 정보
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LogResponseDto> logs = logService.getDailyLogs(user, date);
        return ResponseEntity.ok(logs);
    }

    /* 업무 기록 상세 조회 API */
    @GetMapping("/{logId}")
    public ResponseEntity<LogDetailResponseDto> getLogDetail(
            @PathVariable Long logId) { // URL 경로에 있는 ID를 받아옴
        return ResponseEntity.ok(logService.getLogDetail(logId));
    }
}
