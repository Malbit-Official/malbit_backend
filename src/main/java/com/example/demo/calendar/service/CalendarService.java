package com.example.demo.calendar.service;

import com.example.demo.calendar.dto.CalendarListResponse;
import com.example.demo.calendar.dto.TaskManualRequest;
import com.example.demo.calendar.dto.TaskUpdateRequest;
import com.example.demo.calendar.dto.UpcomingTasksResponse;
import com.example.demo.calendar.repository.TaskRepository;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    @Transactional
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
    @Transactional
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
                                        .is_completed(t.isCompleted())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    /* 일정 상세 수정 로직 */
    @Transactional
    public Long updateTask(Long taskId, TaskUpdateRequest request) {

        // 수정할 일정 조회
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다. [ID: " + taskId + "]"));

        // 엔티티 내부 메서드를 통한 정보 업데이트
        task.update(
                request.getContent(),
                request.getCategory(),
                request.getStart_at(),
                request.getEnd_at()
        );

        return task.getTaskId();
    }

    /* 일정 삭제 로직 */
    @Transactional
    public void deleteTask(Long taskId) {

        // 삭제 대상 존재 여부 확인 후 삭제
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다. [ID: " + taskId + "]"));

        taskRepository.delete(task);
    }

    /* 다가오는 일정 알림 조회 로직 */
    @Transactional
    public UpcomingTasksResponse getUpcomingTasks(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 현재 시간 기준, 아직 시작하지 않았거나 진행 중인 미래 일정 조회 (최대 5개)
        LocalDateTime now = LocalDateTime.now();
        List<Task> upcomingTasks = taskRepository.findAllByUserAndStartAtAfterOrderByStartAtAsc(user, now);

        // DTO 변환 및 남은 시간 계산
        List<UpcomingTasksResponse.UpcomingTaskDto> taskDtos = upcomingTasks.stream()
                .limit(5) // 상위 5개만 노출
                .map(task -> {
                    Duration duration = Duration.between(now, task.getStartAt());
                    long days = duration.toDays();

                    return UpcomingTasksResponse.UpcomingTaskDto.builder()
                            .task_id(task.getTaskId())
                            .content(task.getContent())
                            .category(task.getCategory())
                            .d_day(days) // 남은 일수 저장
                            .remaining_time(formatRemainingTime(now, task.getStartAt()))
                            .build();
                })
                .collect(Collectors.toList());

        return UpcomingTasksResponse.builder()
                .upcoming_tasks(taskDtos)
                .build();
    }

    /* 일정 완료 여부 토글 로직 */
    @Transactional
    public boolean toggleTaskCompletion(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다. [ID: " + taskId + "]"));

        task.toggleCompletion();
        return task.isCompleted();
    }

    // 남은 시간을 "0시간 0분" 형태로 포맷팅하는 헬퍼 메서드
    private String formatRemainingTime(LocalDateTime now, LocalDateTime startAt) {

        Duration duration = Duration.between(now, startAt);
        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();

        if (days >= 1) {
            return "D-" + days; // 1일 이상 남으면 D-Day 표시
        } else if (hours >= 1) {
            return hours + "시간 전"; // 24시간 미만이면 시간 단위
        } else if (minutes > 0) {
            return minutes + "분 전"; // 1시간 미만이면 분 단위
        } else {
            return "잠시 후 시작"; // 시작 임박
        }
    }
}
