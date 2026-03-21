package com.example.demo.dto;

import com.example.demo.domain.CognitiveLevel;
import com.example.demo.domain.DisabilityType;
import com.example.demo.domain.JobType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {
    private JobType jobType; // 직무 분야
    private DisabilityType disabilityType; // 장애 유형
    private CognitiveLevel cognitiveLevel; // 인지 수준
}
