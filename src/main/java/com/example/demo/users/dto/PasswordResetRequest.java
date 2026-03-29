// 비밀번호 재설정 DTO(로그인 전(비밀번호 까먹었을 때) or 회원가입 시)
package com.example.demo.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String email;
    private String password;
    private String passwordConfirm;

    // 비밀번호 일치 확인 로직
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}
