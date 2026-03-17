// 로그인 성공 시 토큰을 담아서 보내줄 바구니(DTO)
package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("is_large_font")
    private boolean isLargeFont;

    private String refreshToken;

    private String email;
}
