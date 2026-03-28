package com.example.demo.users.dto;

import com.example.demo.entity.CognitiveLevel;
import com.example.demo.entity.DisabilityType;
import com.example.demo.entity.JobType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {
    private JobType jobType; // 직무 분야
    private DisabilityType disabilityType; // 장애 유형
    private CognitiveLevel cognitiveLevel; // 인지 수준
}
