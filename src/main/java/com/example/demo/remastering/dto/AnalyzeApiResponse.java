package com.example.demo.remastering.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalyzeApiResponse {

    // FastAPI에서 내려주는 "status" 필드 (예: "SUCCESS")
    private String status;

    // FastAPI에서 내려주는 "data" 객체
    private AnalyzeData data;
}