// JWT 회원가입 DTO
package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    private String email; // 사용자가 입력한 로그인 이메일
    private String password; // 사용자가 입력한 비밀번호
}


