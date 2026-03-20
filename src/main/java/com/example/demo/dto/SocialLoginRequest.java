// 프론트(Native SDK)에서 전달받은 액세스 토큰을 담는 바구니(DTO)
package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialLoginRequest {
    private String provider;
    private String accessToken;
}
