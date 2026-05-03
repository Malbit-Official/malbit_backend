package com.example.demo.calendar.service;

import com.example.demo.calendar.dto.CalendarListResponse;
import com.example.demo.calendar.dto.TaskManualRequest;
import com.example.demo.calendar.repository.TaskRepository;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CalendarService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /* 일정 수동 등록 로직 */
    public Long createManualTask(String email, TaskManualRequest request) {

        // 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // Task 엔티티 빌더 생성
        Task task = Task.builder()
                .user(user)
                .content(request.getContent())
                .startAt(request.getStart_at())
                .endAt(request.getEnd_at())
                .category(request.getCategory())
                .isCompleted(false)
                .build();

        // 저장 및 ID 반환
        return taskRepository.save(task).getTaskId();
    }

    /* 월간/주간 일정 조회 로직 */
    public List<CalendarListResponse> getSchedules(String email, String queryDate) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 조회 기준일 설정 (예: 2026-05-04 -> 5월 전체 조회)
        LocalDate date = LocalDate.parse(queryDate);
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX);

        // 해당 월의 모든 일정 조회
        List<Task> tasks = taskRepository.findAllByUserAndStartAtBetween(user, startOfMonth, endOfMonth);

        // 날짜별로 그룹화하여 DTO 반환
        return tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getStartAt().toLocalDate()))
                .entrySet().stream()
                .map(entry -> CalendarListResponse.builder()
                        .date(entry.getKey().toString())
                        .schedules(entry.getValue().stream()
                                .map(t -> CalendarListResponse.TaskSimpleDto.builder()
                                        .task_id(t.getTaskId())
                                        .content(t.getContent())
                                        .start_at(t.getStartAt())
                                        .end_at(t.getEndAt())
                                        .category(t.getCategory())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
