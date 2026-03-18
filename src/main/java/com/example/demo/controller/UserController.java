// API 호출을 받아 회원 관련 기능을 실행하고 결과를 응답
package com.example.demo.controller;

import com.example.demo.dto.UserLoginResponse;
import com.example.demo.dto.UserJoinRequest;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserInfoResponse;
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

    /* 회원가입 API */
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserJoinRequest request) {
        userService.join(request);
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
