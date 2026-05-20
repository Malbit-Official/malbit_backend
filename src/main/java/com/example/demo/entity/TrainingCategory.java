package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "training_category")
public class TrainingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 카테고리 제목 (예: 주문 받기 연습)

    private String imageUrl; //카드 배경 이미지 URL (S3 경로 등)

    @ElementCollection
    @CollectionTable(
            name = "category_tags",
            joinColumns = @JoinColumn(name = "category_id")
    )
    @Column(name = "tag_name")
    private List<String> tags; // 해시태그 목록 (예: ["#주문", "#확인"])

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScenarioStep> steps;

    @Builder
    public TrainingCategory(String title, String imageUrl, List<String> tags, List<ScenarioStep> steps) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.steps = steps;
    }
}
