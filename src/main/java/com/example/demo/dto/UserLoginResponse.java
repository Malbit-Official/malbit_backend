// 로그인 성공 시 토큰을 담아서 보내줄 바구니(DTO)
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {

    private String accessToken;
    private String refreshToken;
}
