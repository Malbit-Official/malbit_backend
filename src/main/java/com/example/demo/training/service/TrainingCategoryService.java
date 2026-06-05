// 모든 직무 상황 카테고리를 조회하여 반환하는 로직
package com.example.demo.training.service;

import com.example.demo.training.dto.TrainingCategoryResponse;
import com.example.demo.training.repository.TrainingCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainingCategoryService {

    private final TrainingCategoryRepository trainingCategoryRepository;

    public List<TrainingCategoryResponse> getAllCategories() {
        return trainingCategoryRepository.findAll().stream()
                .map(category -> TrainingCategoryResponse.builder()
                        .id(category.getId())
                        .title(category.getTitle())
                        .imageUrl(category.getImageUrl())
                        .tags(category.getTags())
                        .build())
                .collect(Collectors.toList());
    }
}
