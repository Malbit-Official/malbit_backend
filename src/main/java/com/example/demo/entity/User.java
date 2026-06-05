package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // 사용자 id (PK)

    @Column(nullable = false, unique = true)
    private String email; // 로그인 계정

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 성함

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisabilityType disabilityType; // 장애유형

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CognitiveLevel cognitiveLevel; // 인지수준

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType; // 직무분야

    @Column
    private Double speechWaitTime = 1.5; // 말하기 대기 시간 (기본값 1.5초)

    @Enumerated(EnumType.STRING)
    private PreferredTone preferredTone = PreferredTone.POLITE; // 리마스터링 말투 (기본값 존댓말)

    @Enumerated(EnumType.STRING)
    private RegistrationId registrationId; // LOCAL, KAKAO, GOOGLE 중 하나

    @Column(updatable = false)
    private LocalDateTime createdAt; //가입일

    @Column
    private String profileImUrl; // 프로필 사진 이미지 경로

    @Column
    private String voiceFileUrl; // 음성 모델 생성을 위한 원본 음성 파일 경로

    @Column
    private String voiceSampleUrl; // 사전 음성 등록

    @Column
    private String fontSize = "MEDIUM"; // 글자 크기

    @Column
    private Boolean isLargeButton = false; // 버튼 크게 보기 여부

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String password, String name, DisabilityType disabilityType,
                CognitiveLevel cognitiveLevel, JobType jobType, Double speechWaitTime,
                PreferredTone preferredTone, RegistrationId registrationId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.disabilityType = disabilityType;
        this.cognitiveLevel = cognitiveLevel;
        this.jobType = jobType;
        this.speechWaitTime = (speechWaitTime != null) ? speechWaitTime : 1.5;
        this.preferredTone = (preferredTone != null) ? preferredTone : PreferredTone.POLITE;
        this.registrationId = registrationId;
        this.fontSize = "MEDIUM";
        this.isLargeButton = false;
    }

    // 비밀번호 변경
    public void updatePassword(String password) {
        this.password = password;
    }

    // 이메일 변경
    public void updateEmail(String email) { this.email = email; }

    // 프로필 사진 추가
    public void updateProfileImage(String profileImUrl) {
        this.profileImUrl = profileImUrl;
    }

    // 환경 설정
    public void updateEnvironment(JobType jobType, DisabilityType disabilityType, CognitiveLevel cognitiveLevel) {
        if (jobType != null) this.jobType = jobType;
        if (disabilityType != null) this.disabilityType = disabilityType;
        if (cognitiveLevel != null) this.cognitiveLevel = cognitiveLevel;
    }

    // 음성 재등록, 삭제
    public void updateVoiceFile(String voiceFileUrl) {
        this.voiceFileUrl = voiceFileUrl;
    }

    // 사전 음성 등록
    public void updateVoiceSample(String voiceSampleUrl) {
        this.voiceSampleUrl = voiceSampleUrl;
    }

    // 음성 인식 및 말투 설정
    public void updateAiSettings(Double speechWaitTime, PreferredTone preferredTone) {
        if (speechWaitTime != null) this.speechWaitTime = speechWaitTime;
        if (preferredTone != null) this.preferredTone = preferredTone;
    }

    // 사용자 디스플레이 설정
    public void updateDisplaySettings(String fontSize, Boolean isLargeButton) {
        if (fontSize != null) this.fontSize = fontSize;
        if (isLargeButton != null) this.isLargeButton = isLargeButton;
    }

    public User updateName(String name) {
        this.name = name;
        return this;
    }

    public enum RegistrationId {
        LOCAL, KAKAO, GOOGLE
    }

}