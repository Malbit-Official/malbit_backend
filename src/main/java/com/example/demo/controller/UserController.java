package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.EmailService;
import com.example.demo.service.FileService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final FileService fileService;

    /* 회원가입 API */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Object>> join(@RequestBody UserJoinRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("입력하신 비밀번호가 서로 일치하지 않습니다."));
        }
        if (request.getJobType() == null || request.getDisabilityType() == null || request.getCognitiveLevel() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("직무 환경, 장애 유형, 인지 수준은 필수 입력 사항입니다."));
        }
        if (!emailService.isVerified(request.getEmail())) {
            return ResponseEntity.status(400).body(ApiResponse.fail("이메일 인증이 완료되지 않았습니다."));
        }

        userService.join(request);
        emailService.removeVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    /* 로그인 API */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserLoginResponse>> login(@RequestBody UserLoginRequest request) {
        try {
            UserLoginResponse response = userService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    /* 비밀번호 재설정 API */
    @PatchMapping("/password/reset")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@RequestBody PasswordResetRequest request) {
        if (!request.isPasswordMatching()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("입력하신 두 비밀번호가 서로 일치하지 않습니다."));
        }
        if (!emailService.isReadyToReset(request.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("이메일 인증이 확인되지 않았습니다."));
        }

        try {
            userService.resetPassword(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(ApiResponse.fail(e.getMessage()));
        }
    }

    /* 토큰 인증 테스트 API */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserInfoResponse response = userService.getUserInfo(email);
        return ResponseEntity.ok(ApiResponse.success("정보가 성공적으로 조회되었습니다.", response));
    }

    /* 비밀번호 변경 API */
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Object>> updatePassword(
            @AuthenticationPrincipal String email,
            @RequestBody PasswordUpdateRequest request) {
        try {
            userService.updatePassword(email, request);
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    /* 이메일 변경 API */
    @PatchMapping("/email")
    public ResponseEntity<ApiResponse<Object>> updateEmail(
            @AuthenticationPrincipal String email,
            @RequestBody EmailUpdateRequest request) {
        if (!emailService.isVerified(request.getNewEmail())) {
            return ResponseEntity.status(400).body(ApiResponse.fail("새 이메일 인증이 완료되지 않았습니다."));
        }

        try {
            userService.updateEmail(email, request.getNewEmail());
            emailService.removeVerification(request.getNewEmail());
            return ResponseEntity.ok(ApiResponse.success("이메일이 성공적으로 변경되었습니다. 다시 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    /* 프로필 사진 업로드 API */
    @PostMapping("/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @AuthenticationPrincipal String email,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileService.saveFile(file, "profiles");
            userService.updateProfileImage(email, imageUrl);
            return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 업로드되었습니다.", Map.of("imageUrl", imageUrl)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("파일 저장 중 오류 발생"));
        }
    }

    /* 사용자 환경 설정 변경 API */
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateEnvironment(
            @AuthenticationPrincipal String email,
            @RequestBody ProfileUpdateRequest request) {
        try {
            userService.updateEnvironment(email, request);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("job_type", request.getJobType() != null ? request.getJobType() : "기존유지");
            data.put("disability_type", request.getDisabilityType() != null ? request.getDisabilityType() : "기존유지");
            data.put("cognitive_level", request.getCognitiveLevel() != null ? request.getCognitiveLevel() : "기존유지");

            return ResponseEntity.ok(ApiResponse.success("환경 설정이 변경되었습니다.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    /* 음성 재등록 API */
    @PostMapping("/voice/re-register")
    public ResponseEntity<ApiResponse<Object>> reRegisterVoice(
            @AuthenticationPrincipal String email,
            @RequestParam("voiceFiles") List<MultipartFile> voiceFiles) {
        try {
            if (voiceFiles == null || voiceFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("등록할 음성 파일이 없습니다."));
            }
            userService.reRegisterVoiceFiles(email, voiceFiles);
            return ResponseEntity.ok(ApiResponse.success("새로운 음성 데이터가 등록되었습니다. AI 학습을 시작합니다."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("파일 저장 중 오류가 발생했습니다."));
        }
    }

    /* 음성 삭제 API */
    @DeleteMapping("/voice")
    public ResponseEntity<ApiResponse<Object>> deleteVoice(@AuthenticationPrincipal String email) {
        try {
            userService.deleteVoiceFiles(email);
            return ResponseEntity.ok(ApiResponse.success("등록된 음성 데이터가 모두 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("음성 삭제 중 오류가 발생했습니다."));
        }
    }

    /* 음성 인식 및 말투 설정 API */
    @PatchMapping("/ai-settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateAiSettings(
            @AuthenticationPrincipal String email,
            @RequestBody AiSettingsRequest request) {
        try {
            userService.updateAiSettings(email, request);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("speechWaitTime", request.getSpeechWaitTime());
            data.put("preferredTone", request.getPreferredTone());

            return ResponseEntity.ok(ApiResponse.success("음성 인식 및 말투 설정이 저장되었습니다.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("설정 저장 중 오류가 발생했습니다."));
        }
    }

    /* 사전 음성 등록 API */
    @PostMapping("/voice/samples")
    public ResponseEntity<ApiResponse<Object>> registerVoiceSamples(
            @AuthenticationPrincipal String email,
            @RequestParam("voiceFiles") List<MultipartFile> voiceFiles) {
        try {
            if (voiceFiles == null || voiceFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("등록할 음성 샘플 파일이 없습니다."));
            }
            userService.registerVoiceSamples(email, voiceFiles);
            return ResponseEntity.ok(ApiResponse.success("사전 음성 샘플이 성공적으로 등록되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("파일 저장 중 오류가 발생했습니다."));
        }
    }

    /* 사용자 디스플레이 설정 API */
    @PatchMapping("/display-settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateDisplaySettings(
            @AuthenticationPrincipal String email,
            @RequestBody DisplaySettingsRequest request) {
        try {
            userService.updateDisplaySettings(email, request);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("fontSize", request.getFontSize());
            data.put("isLargeButton", request.getIsLargeButton());

            return ResponseEntity.ok(ApiResponse.success("사용자 디스플레이 설정이 저장되었습니다.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("설정 저장 중 오류가 발생했습니다."));
        }
    }

    /* 통계 데이터 조회 API */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getStatistics(@AuthenticationPrincipal String email) {
        try {
            UserStatisticsResponse data = userService.getStatistics(email);
            return ResponseEntity.ok(ApiResponse.success("통계 데이터를 성공적으로 조회했습니다.", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("통계 조회 중 오류가 발생했습니다."));
        }
    }
}