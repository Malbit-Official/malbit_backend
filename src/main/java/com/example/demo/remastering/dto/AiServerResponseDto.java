package com.example.demo.remastering.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiServerResponseDto {
    private String rawText;
    private String refinedText;
}
