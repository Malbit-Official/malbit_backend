package com.example.demo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PreferredTone {
    FORMAL("격식체 (하십시오체)"),
    POLITE("해요체 (존댓말)"),
    CASUAL("해체 (반말/일상체)");

    private final String description;
}