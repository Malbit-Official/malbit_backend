package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserJoinResponse {
    private String email;
    private String nickname;
}
