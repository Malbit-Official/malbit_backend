// 이메일 변경 DTO
package com.example.demo.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailUpdateRequest {
    private String newEmail;
}
