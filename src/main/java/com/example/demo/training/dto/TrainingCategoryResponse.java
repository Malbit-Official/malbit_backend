// 직무 상황 카테고리 전체 조회
package com.example.demo.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class TrainingCategoryResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private List<String> tags;
}
