// 회원가입, 로그인, 비밀번호 재설정 등 유저 정보와 관련된 기능을 처리하는 컨트롤러
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
    public ResponseEntity<?> join(@RequestBody UserJoinRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "Meessage", "입력하신 비밀번호가 서로 일치하지 않습니다."));
        }

        if (!emailService.isVerified(request.getEmail())) {
            return ResponseEntity.status(400).body(Map.of("status", "FAIL", "message", "이메일 인증이 완료되지 않았습니다."));
        }

        userService.join(request);
        emailService.removeVerification(request.getEmail());
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "회원가입이 완료되었습니다."));
    }

    /* 로그인 API */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {

        try {
            UserLoginResponse response = userService.login(request.getEmail(), request.getPassword());

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "messsage", "로그인이 완료되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "message", e.getMessage()));
        }

    }

    /* 비밀번호 재설정 API */
    @PatchMapping("/password/reset") // 최종 주소: /api/users/password/reset
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (!request.isPasswordMatching()) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "message", "입력하신 두 비밀번호가 서로 일치하지 않습니다."));
        }

        if (!emailService.isReadyToReset(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "message", "이메일 인증이 확인되지 않았습니다."));
        }

        try {
            userService.resetPassword(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("status", "FAIL", "message", e.getMessage()));
        }
    }

    /* 토큰 인증 테스트 API */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfoResponse response = userService.getUserInfo(email);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "정보가 성공적으로 조회되었습니다.",
                "data", response
        ));
    }

    /* 비밀번호 변경 API */
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal String email,
            @RequestBody PasswordUpdateRequest request) {

        try {
            userService.updatePassword(email, request);
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "message", e.getMessage()));
        }
    }

    /* 이메일 변경 API */
    @PatchMapping("/email")
    public ResponseEntity<?> updateEmail(
            @AuthenticationPrincipal String email,
            @RequestBody EmailUpdateRequest request) {

        // 새 이메일 인증되었는지 확인
        if (!emailService.isVerified(request.getNewEmail())) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "FAIL",
                    "message", "새 이메일 인증이 완료되지 않았습니다."
            ));
        }

        try {
            userService.updateEmail(email, request.getNewEmail());

            // 인증 완료 후 저장된 인증 정보 삭제 (1회용)
            emailService.removeVerification(request.getNewEmail());

            // 이메일이 바뀌면 기존 토큰의 정보가 무효해지므로 다시 로그인을 유도
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "이메일이 성공적으로 변경되었습니다. 다시 로그인해주세요."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        }
    }

    /* 프로필 사진 업로드 API */
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal String email,
            @RequestParam("file") MultipartFile file) {
        try {
            // 파일 저장
            String subDir = "profiles";
            String imageUrl = fileService.saveFile(file, subDir);

            // DB에 이미지 URL 업데이트
            userService.updateProfileImage(email, imageUrl);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "프로필 이미지가 업로드되었습니다.",
                    "data", Map.of("imageUrl", imageUrl)
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "FAIL", "message", "파일 저장 중 오류 발생"));
        }
    }

    /* 사용자 환경 설정 변경 API */
    @PatchMapping("/settings")
    public ResponseEntity<?> updateEnvironment(
            @AuthenticationPrincipal String email,
            @RequestBody ProfileUpdateRequest request) {

        try {
            userService.updateEnvironment(email, request);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "환경 설정이 변경되었습니다.",
                    "data", Map.of(
                            "job_type", request.getJobType() != null ? request.getJobType() : "기존유지",
                            "disability_type", request.getDisabilityType() != null ? request.getDisabilityType() : "기존유지",
                            "cognitive_level", request.getCognitiveLevel() != null ? request.getCognitiveLevel() : "기존유지"
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        }
    }

    /* 음성 재등록 API */
    @PostMapping("/voice/re-register")
    public ResponseEntity<?> reRegisterVoice(
            @AuthenticationPrincipal String email,
            @RequestParam("voiceFiles") List<MultipartFile> voiceFiles) {

        try {
            if (voiceFiles == null || voiceFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "FAIL", "message", "등록할 음성 파일이 없습니다."));
            }

            userService.reRegisterVoiceFiles(email, voiceFiles);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "새로운 음성 데이터가 등록되었습니다. AI 학습을 시작합니다."
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAIL",
                    "message", "파일 저장 중 오류가 발생했습니다."
            ));
        }
    }

    /* 음성 삭제 API */
    @DeleteMapping("/voice")
    public ResponseEntity<?> deleteVoice(@AuthenticationPrincipal String email) {
        try {
            userService.deleteVoiceFiles(email);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "등록된 음성 데이터가 모두 삭제되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAIL",
                    "message", "음성 삭제 중 오류가 발생했습니다."
            ));
        }
    }

    /* 음성 인식 및 말투 설정 API */
    @PatchMapping("/ai-settings")
    public ResponseEntity<?> updateAiSettings(
            @AuthenticationPrincipal String email,
            @RequestBody AiSettingsRequest request) {

        try {
            userService.updateAiSettings(email, request);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "음성 인식 및 말투 설정이 저장되었습니다.",
                    "data", Map.of(
                            "speechWaitTime", request.getSpeechWaitTime(),
                            "preferredTone", request.getPreferredTone()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", "설정 저장 중 오류가 발생했습니다."
            ));
        }
    }

    /* 사전 음성 등록 API */
    @PostMapping("/voice/samples")
    public ResponseEntity<?> registerVoiceSamples(
            @AuthenticationPrincipal String email,
            @RequestParam("voiceFiles") List<MultipartFile> voiceFiles) {

        try {
            if (voiceFiles == null || voiceFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "FAIL",
                        "message", "등록할 음성 샘플 파일이 없습니다."
                ));
            }

            userService.registerVoiceSamples(email, voiceFiles);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "사전 음성 샘플이 성공적으로 등록되었습니다."
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAIL",
                    "message", "파일 저장 중 오류가 발생했습니다."
            ));
        }
    }
}

