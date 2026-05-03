package com.example.demo.calendar.service;

import com.example.demo.calendar.dto.TaskManualRequest;
import com.example.demo.calendar.repository.TaskRepository;
import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CalendarService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

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
}
