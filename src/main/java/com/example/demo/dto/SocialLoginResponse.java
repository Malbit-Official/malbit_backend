// 서버에서 발행한 JWT와 신규 유저 여부를 반환
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SocialLoginResponse {

    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private String message;
}
