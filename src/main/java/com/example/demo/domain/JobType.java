package com.example.demo.domain;

import lombok.Getter;

@Getter
public enum JobType {
    OFFICE("사무직"),
    SALES("영업 / 고객상담"),
    MEDICAL("의료 / 간호"),
    EDUCATION("교육 / 학교"),
    SERVICE("서비스 / 매장"),
    ETC("기타");

    private final String description;

    JobType(String description) {
        this.description = description;
    }
}
