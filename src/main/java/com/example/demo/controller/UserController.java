// 회원가입, 로그인, 비밀번호 재설정 등 유저 정보와 관련된 기능을 처리하는 컨트롤러
package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    /* 회원가입 API */
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody UserJoinRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            return ResponseEntity.badRequest().body(Map.of("message", "입력하신 비밀번호가 서로 일치하지 않습니다."));
        }

        if (!emailService.isVerified(request.getEmail())) {
            return ResponseEntity.status(400).body(Map.of("message", "이메일 인증이 완료되지 않았습니다."));
        }

        userService.join(request);
        emailService.removeVerification(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    /* 로그인 API */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {

        UserLoginResponse response = userService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(response);
    }

    /* 비밀번호 재설정 API */
    @PatchMapping("/password/reset") // 최종 주소: /api/users/password/reset
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        if (!request.isPasswordMatching()) {
            return ResponseEntity.badRequest().body(Map.of("message", "입력하신 두 비밀번호가 서로 일치하지 않습니다."));
        }

        if (!emailService.isReadyToReset(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일 인증이 확인되지 않았습니다."));
        }

        try {
            userService.updatePassword(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /* 토큰 인증 테스트 API */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfoResponse response = userService.getUserInfo(email);

        return ResponseEntity.ok(response);
    }
}
