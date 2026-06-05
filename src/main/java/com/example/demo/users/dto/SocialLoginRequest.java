// 소셜 로그인 DTO
package com.example.demo.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {
    private String provider;
    private String accessToken;
}
