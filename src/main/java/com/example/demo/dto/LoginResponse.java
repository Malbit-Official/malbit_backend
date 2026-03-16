// 로그인 성공 시 토큰을 담아서 보내줄 바구니(DTO)
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
}
