package com.example.demo.calendar.controller;

import com.example.demo.calendar.dto.CalendarListResponse;
import com.example.demo.calendar.dto.TaskManualRequest;
import com.example.demo.calendar.dto.TaskUpdateRequest;
import com.example.demo.calendar.dto.UpcomingTasksResponse;
import com.example.demo.calendar.service.CalendarService;
import com.example.demo.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "캘린더 관리", description = "사용자 일정 조회 및 관리 기능을 제공합니다.")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

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

    /* 일정 상세 수정 API */
    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request) {

        Long updateId = calendarService.updateTask(taskId, request);

        return ResponseEntity.ok(ApiResponse.success("일정이 수정되었습니다.",
                Map.of("task_id", updateId, "status", "updated")));

    }

    /* 일정 삭제 API */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable Long taskId) {

        calendarService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success("해당 일정이 삭제되었습니다."));
    }

    /* 다가오는 일정 알림 조회 API */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<UpcomingTasksResponse>> getUpcomingTasks(
            @AuthenticationPrincipal String email) {

        UpcomingTasksResponse response = calendarService.getUpcomingTasks(email);
        return ResponseEntity.ok(ApiResponse.success("임박 일정 조회 성공", response));
    }

    /* 일정 완료 여부 토글 API (통일된 버전) */
    @PatchMapping("/{taskId}/completion")
    public ResponseEntity<ApiResponse<Boolean>> toggleTaskCompletion(@PathVariable Long taskId) {

        // 서비스에서 toggle 로직 호출 (DB 업데이트)
        boolean updatedStatus = calendarService.toggleTaskCompletion(taskId);

        return ResponseEntity.ok(ApiResponse.success(
                "일정 상태가 " + (updatedStatus ? "완료" : "미완료") + "로 변경되었습니다.",
                updatedStatus
        ));
    }

}
