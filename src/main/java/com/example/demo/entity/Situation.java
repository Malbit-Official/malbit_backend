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
    private Long situationId; // 상황ID (PK)

    @Column(nullable = false)
    private String situationName; // 상황명

    private String locationTag; //위치태그

    private String iconUrl; //아이콘이미지

    @Column(nullable = false)
    private String keyword; // 핵심 키워드

    private String bgColor; // 상황별 테마 색상

    @Builder
    public Situation(String situationName, String locationTag, String iconUrl, String keyword, String bgColor) {
        this.situationName = situationName;
        this.locationTag = locationTag;
        this.iconUrl = iconUrl;
        this.keyword = keyword;
        this.bgColor = bgColor;
    }
}