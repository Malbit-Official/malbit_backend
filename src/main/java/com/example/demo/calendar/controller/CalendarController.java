package com.example.demo.calendar.controller;

import com.example.demo.calendar.dto.CalendarListResponse;
import com.example.demo.calendar.dto.TaskManualRequest;
import com.example.demo.calendar.service.CalendarService;
import com.example.demo.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private CalendarService calendarService;

    /* 일정 수동 등록 API */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createManualTask(
            @AuthenticationPrincipal String email,
            @RequestBody TaskManualRequest request) {

        Long taskId = calendarService.createManualTask(email, request);

        return ResponseEntity.ok(ApiResponse.success("일정이 성공적으로 등록되었습니다.", Map.of("task_id", taskId)));
    }

    /* 월간/주간 일정 조회 API */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarListResponse>>> getSchedules(
            @AuthenticationPrincipal String email,
            @RequestParam("query_date") String queryDate) {

        List<CalendarListResponse> response = calendarService.getSchedules(email, queryDate);
        return ResponseEntity.ok(ApiResponse.success("일정 조회 성공", response));
    }
}
