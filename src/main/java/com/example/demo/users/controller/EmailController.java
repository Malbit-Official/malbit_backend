package com.example.demo.users.controller;

import com.example.demo.global.common.ApiResponse;
import com.example.demo.users.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /* 이메일 인증번호 발송 API */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Object>> sendEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("이메일 주소를 입력해주세요."));
            }

            emailService.sendEmail(email);
            return ResponseEntity.ok(ApiResponse.success("인증번호가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.fail("메일 발송 중 오류가 발생했습니다."));
        }
    }

    /* 이메일 인증번호 검증 API */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Object>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("이메일과 인증번호를 모두 입력해주세요."));
        }

        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(ApiResponse.success("이메일 인증에 성공하였습니다."));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }
}