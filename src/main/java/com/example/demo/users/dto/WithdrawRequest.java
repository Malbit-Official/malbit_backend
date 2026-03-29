package com.example.demo.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRequest {
    private String password; // 탈퇴 확인용 비밀번호
}