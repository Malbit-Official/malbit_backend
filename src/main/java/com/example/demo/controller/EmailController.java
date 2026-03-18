// 로그인 전, 이메일 인증번호를 발송하고 확인하는 인증 전담 컨트롤러
package com.example.demo.controller;

import com.example.demo.service.EmailService;
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

    /* 인증번호 발송 API */
    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String, String> request) {
        emailService.sendEmail(request.get("email"));
        return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다."));
    }

    /* 인증번호 확인 API */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        boolean isVerified = emailService.verifyCode(request.get("email"), request.get("code"));
        if (isVerified) {
            return ResponseEntity.ok(Map.of("message", "인증 성공!"));
        }
        return ResponseEntity.badRequest().body(Map.of("message", "인증번호가 일치하지 않습니다."));
    }

}
