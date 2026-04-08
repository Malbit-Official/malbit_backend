// 업무 기록 목록 조회
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate date; // 기록된 날짜 (예: 2026-04-07)

    private LocalTime startTime; // 시작 시간 (예: 10:15)

    private String duration; // 소요 시간 (예: "15분 29초")

    @Enumerated(EnumType.STRING)
    private LogType type; // CONFERENCE, TASK 등 (구분용)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 작성자 연결

    private String memo;

    // 메모 수정을 위한 메서드
    public void updateMemo(String memo) {
        this.memo = memo;
    }
}
