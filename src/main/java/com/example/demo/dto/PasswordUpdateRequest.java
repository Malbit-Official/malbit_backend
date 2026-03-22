// 비밀번호 변경 DTO(프로필 관리에서 바꿀 때)
package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequest {
    private String oldPassword; // 기존 비번
    private String newPassword; // 바꿀 비번
}
