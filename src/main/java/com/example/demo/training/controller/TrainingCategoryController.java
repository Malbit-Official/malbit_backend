package com.example.demo.training.controller;

import com.example.demo.global.common.ApiResponse;
import com.example.demo.training.dto.TrainingCategoryResponse;
import com.example.demo.training.service.TrainingCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingCategoryController {

    private final TrainingCategoryService trainingCategoryService;

    /* 직무 상황 카테고리 전체 조회 API */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<TrainingCategoryResponse>>> getCatogories() {
        return ResponseEntity.ok(
                ApiResponse.success("카테고리 목록 조회 성공하였습니다.", trainingCategoryService.getAllCategories())
        );
    }
}