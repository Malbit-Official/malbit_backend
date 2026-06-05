package com.example.demo.remastering.controller;

import com.example.demo.remastering.dto.MeetingAnalysisResponse;
import com.example.demo.remastering.dto.RemasteringLogResponse;
import com.example.demo.remastering.service.RemasteringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import com.example.demo.global.common.ApiResponse;

@Tag(name = "문장 리마스터링", description = "음성 파일을 분석하여 정제된 문장으로 변환합니다.")
@RestController
@RequestMapping("/api/remaster")
public class RemasteringController {

    private final RemasteringService remasteringService;

    public RemasteringController(RemasteringService remasteringService) {
        this.remasteringService = remasteringService;
    }

    @Operation(
            summary = "문장 리마스터링",
            description = "음성 파일과 원하는 말투를 받아 리마스터링 결과를 반환하고 DB에 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "리마스터링 성공",
                            content = @Content(schema = @Schema(implementation = RemasteringLogResponse.class))
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse<RemasteringLogResponse>>> createLog (
            @AuthenticationPrincipal String email,

            @Parameter(description = "업로드할 음성 파일")
            @RequestPart("audio_file") MultipartFile audioFile,

            @Parameter(description = "원하는 말투", example = "gentle")
            @RequestPart(value = "preferred_tone", required = false) String preferredTone

    ) {
        return remasteringService.remaster(email, audioFile, preferredTone)
                .map(result -> ResponseEntity.ok(ApiResponse.success("리마스터링 성공", result)));
    }

    @Operation(
            summary = "회의록 분석 및 일정 추출",
            description = "회의 음성 파일을 분석하여 요약, 체크리스트, 일정을 반환하고 DB에 저장합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    @PostMapping(value = "/analyze-meeting", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse<MeetingAnalysisResponse>>> analyzeMeeting(
            @AuthenticationPrincipal String email,

            @Parameter(description = "업로드할 회의 음성 파일")
            @RequestPart("audio_file") MultipartFile audioFile
    ) {

        return remasteringService.analyzeMeeting(email, audioFile)
                .map(result -> ResponseEntity.ok(ApiResponse.success("회의 분석 성공", result)));
    }
}