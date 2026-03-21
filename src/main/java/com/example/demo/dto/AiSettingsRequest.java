// 음성 인식 및 말투 설정
package com.example.demo.dto;

import com.example.demo.domain.PreferredTone;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiSettingsRequest {
    private Double speechWaitTime; // 말하기 대기 시간
    private PreferredTone preferredTone; // 리마스터링 말투
}