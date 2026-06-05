// 직무 상황 카테고리 전체 조회
package com.example.demo.training.dto;

import com.example.demo.entity.TrainingCategory;
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

    public static TrainingCategoryResponse from(TrainingCategory category) {
        return TrainingCategoryResponse.builder()
                .id(category.getId())
                .title(category.getTitle())
                .imageUrl(category.getImageUrl())
                .tags(category.getTags())
                .build();
    }
}
