// API 호출을 받아 회원 관련 기능을 실행하고 결과를 응답
package com.example.demo.controller;

import com.example.demo.dto.UserLoginResponse;
import com.example.demo.dto.UserJoinRequest;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserInfoResponse;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final EmailService emailService;

    /* 회원가입 API */
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserJoinRequest request) {

        // 이메일 인증 여부 확인
        if (!emailService.isVerified(request.getEmail())) {
            return ResponseEntity.status(400).body("이메일 인증이 완료되지 않았습니다.");
        }

        // 인증되었다면 회원가입 로직 실행
        userService.join(request);

        //  가입 완료 후 인증 기록 삭제
        emailService.removeVerification(request.getEmail());

        return ResponseEntity.ok("회원가입 성공");
    }

    /* 로그인 API */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {

        UserLoginResponse response = userService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(response);
    }

    /* 토큰 인증 테스트 API */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserInfoResponse response = userService.getUserInfo(email);

        return ResponseEntity.ok(response);
    }
}
