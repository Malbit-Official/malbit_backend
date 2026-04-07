package com.example.demo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogType {

    CONFERENCE("회의 요약"),
    TASK("업무 기록"),
    MEMO("일반 메모");

    private final String description;
}
