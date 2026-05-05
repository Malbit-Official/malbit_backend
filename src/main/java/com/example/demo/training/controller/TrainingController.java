package com.example.demo.training.controller;

import com.example.demo.entity.User;
import com.example.demo.global.common.ApiResponse;
import com.example.demo.training.dto.*;
import com.example.demo.training.service.TrainingCategoryService;
import com.example.demo.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingCategoryService trainingCategoryService;
    private final TrainingService trainingService;

    /* 직무 상황 카테고리 전체 조회 API */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TrainingCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(
                ApiResponse.success("카테고리 목록 조회 성공하였습니다.", trainingCategoryService.getAllCategories())
        );
    }

    /* 특정 직무 연습 시작 API */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TrainingStartResponse>> startTraining(
            @RequestBody TrainingStartRequest request,
            @AuthenticationPrincipal String email
            ) {
        TrainingStartResponse response = trainingService.startTraining(request, email);
        return ResponseEntity.ok(ApiResponse.success("연습 세션이 시작되었습니다.", response));
    }

    /* 발음 분석 및 다음 단계 진행 API */
    @PostMapping("/step")
    public ResponseEntity<ApiResponse<TrainingStepResponse>> proceedStep(
            @RequestBody TrainingStepRequest request,
            @AuthenticationPrincipal String email
    ) {
        // 발음 분석과 다음 데이터 조회를 한 번에 처리해서 응답
        TrainingStepResponse response = trainingService.processStep(request, email);
        return ResponseEntity.ok(ApiResponse.success("단계가 갱신되었습니다.", response));
    }

    /* 실시간 음성 분석 API */
    @PostMapping(value = "/analyze-voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> analyzeVoice(
            @RequestParam("audio_file") MultipartFile audioFile,
            @RequestParam("sessionId") Long sessionId,
            @AuthenticationPrincipal String email
    ) {
        String finalRefinedText = trainingService.processFullCycle(audioFile, sessionId, email);

        // 최종 분석 결과 반환
        return ResponseEntity.ok(ApiResponse.success("음성 분석 및 문장 정제가 완료되었습니다.", finalRefinedText));
    }

    /* 연습 종료 및 결과 저장 API */
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ApiResponse<TrainingResultResponse>> finishTraining(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal String email
    ) {
        TrainingResultResponse response = trainingService.finishTraining(sessionId, email);
        return ResponseEntity.ok(ApiResponse.success("연습이 완료되었습니다.", response));
    }
}