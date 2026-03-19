package com.example.demo.dto;

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
