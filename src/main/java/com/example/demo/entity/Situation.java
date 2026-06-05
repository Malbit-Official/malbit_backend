package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "situation")
public class Situation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "situation_id")
    private Long id;

    @Column(nullable = false)
    private String situationName; // 프론트 버튼용 명칭 (예: "상품 안내 중")

    private String locationTag; // 위치 정보 (예: "매장", "카페")

    private String iconUrl;

    private String bgColor;

    @Column(nullable = false)
    private String keyword; // 해시태그용 키워드 (예: "재고, 안내")

    @Column(columnDefinition = "TEXT")
    private String baseContext;  // AI에게 전달할 핵심 상황 설명 (프롬프트 베이스)

    @Builder
    public Situation(String situationName, String locationTag, String iconUrl,
                     String keyword, String bgColor, String baseContext) {
        this.situationName = situationName;
        this.locationTag = locationTag;
        this.iconUrl = iconUrl;
        this.keyword = keyword;
        this.bgColor = bgColor;
        this.baseContext = baseContext;
    }
}