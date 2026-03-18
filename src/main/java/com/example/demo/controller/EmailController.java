// 사용자로부터 이메일 인증 요청을 받고, 서비스 계층을 호출아여 인증번호 발송 및 확인 결과 반환
package com.example.demo.controller;

import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
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

    // 인증번호 발송 API
    @PostMapping("/send")
    public String sendEmail(@RequestBody Map<String, String> request) {
        emailService.sendEmail(request.get("email"));
        return "인증번호가 발송되었습니다.";
    }

    // 인증번호 확인 API
    @PostMapping("/verify")
    public String verifyCode(@RequestBody Map<String, String> request) {
        boolean isVerified = emailService.verifyCode(request.get("email"), request.get("code"));
        return isVerified ? "인증 성공!" : "인증번호가 일치하지 않습니다.";
    }

}
