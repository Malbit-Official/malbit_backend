package com.example.demo.users.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DisplaySettingsRequest {
    private String fontSize;      // 글자 크기 ( "SMALL", "MEDIUM", "LARGE" )
    private Boolean isLargeButton; // 버튼 크게 보기 여부
}