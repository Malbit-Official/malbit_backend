package com.example.demo.dto;

import lombok.Getter;

@Getter
public class UserSocialRequest {
    private String provider;    // kakao 혹은 google
    private String code;    // 프론트가 받아온 인가 코드
}

