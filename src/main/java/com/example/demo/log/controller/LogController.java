package com.example.demo.log.controller;

import com.example.demo.entity.LogDetail;
import com.example.demo.entity.User;
import com.example.demo.global.common.ApiResponse;
import com.example.demo.log.dto.LogCreateRequest;
import com.example.demo.log.dto.LogDetailResponseDto;
import com.example.demo.log.dto.LogResponseDto;
import com.example.demo.log.dto.MemoRequest;
import com.example.demo.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<List<LogResponseDto>>> getDailyLogs(
            @AuthenticationPrincipal String email, // 로그인된 유저 정보
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LogResponseDto> logs = logService.getDailyLogs(email, date);
        return ResponseEntity.ok(ApiResponse.success("업무 기록 목록 조회 성공", logs));
    }

    /* 업무 기록 상세 조회 API */
    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<LogDetailResponseDto>> getLogDetail(
            @PathVariable Long logId) { // URL 경로에 있는 ID를 받아옴

        LogDetailResponseDto response = logService.getLogDetail(logId);
        return ResponseEntity.ok(ApiResponse.success("업무 기록 상세 조회 성공", response));
    }

    /* 업무 생성 및 요약 API */
    @PostMapping
    public ResponseEntity<ApiResponse<LogDetailResponseDto>> createLog(
            @AuthenticationPrincipal String email,
            @RequestBody LogCreateRequest request) {

        LogDetailResponseDto response = logService.createLog(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("업무 기록이 생성 및 요약되었습니다.", response));
    }

    /* 메모 추가 및 수정 API */
    @PatchMapping("/{logId}/memo")
    public ResponseEntity<ApiResponse<Object>> updateMemo(
            @PathVariable Long logId,
            @RequestBody MemoRequest request) {

        logService.updateMemo(logId, request.getMemo());
        return ResponseEntity.ok(ApiResponse.success("메모가 수정되었습니다."));
    }
}
