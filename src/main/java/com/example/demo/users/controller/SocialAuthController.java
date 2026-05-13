// Native SDK 방식을 사용하는 앱 클라이언트를 위한 인증 컨트롤러
package com.example.demo.users.controller;

import com.example.demo.users.dto.SocialLoginRequest;
import com.example.demo.users.service.SocialAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@Tag(name = "소셜 인증", description = "구글, 카카오 등 외부 소셜 계정 연동 및 로그인을 처리합니다.")
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/social")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginRequest request) {
        String jwt = socialAuthService.login(request.getProvider(), request.getAccessToken());
        return ResponseEntity.ok(Map.of("accessToken", jwt));
    }
}
