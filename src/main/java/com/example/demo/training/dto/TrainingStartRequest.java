// 어떤 연습 카테고리를 선택할 지 확인
package com.example.demo.training.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrainingStartRequest {
    private Long categoryId;
}
