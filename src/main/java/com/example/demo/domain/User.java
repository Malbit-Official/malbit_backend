package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성을 막아 보안성 강화
@Table(name = "users") // DB 예약어인 'user'의 충돌 방지를 위해 테이블명 명시
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
    private DisabilityType disabilityType; // 장애유형

    private Integer cognitiveLevel; // 인지수준

    @Enumerated(EnumType.STRING)
    private JobType jobType; // 직무분야

    private String preferredTone; // 리마스터링 말투

    @Enumerated(EnumType.STRING)
    private RegistrationId registrationId; // LOCAL, KAKAO, GOOGLE 중 하나

    @Column(updatable = false)
    private LocalDateTime createdAt; //가입일

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String password, String name, DisabilityType disabilityType,
                Integer cognitiveLevel, JobType jobType, String preferredTone, RegistrationId registrationId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.disabilityType = disabilityType;
        this.cognitiveLevel = cognitiveLevel;
        this.jobType = jobType;
        this.preferredTone = preferredTone;
        this.registrationId = registrationId;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public User updateName(String name) {
        this.name = name;
        return this;
    }

    public enum RegistrationId {
        LOCAL, KAKAO, GOOGLE
    }


}