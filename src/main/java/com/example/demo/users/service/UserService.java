// 회원가입 및 로그인 등 회원 관련 핵심 비즈니스 로직을 처리하는 클래스
package com.example.demo.users.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserStatistics;
import com.example.demo.global.infrastructure.FileService;
import com.example.demo.users.dto.*;
import com.example.demo.users.repository.UserRepository;
import com.example.demo.users.repository.UserStatisticsRepository;
import com.example.demo.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FileService fileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserStatisticsRepository statisticsRepository;


    /* 회원가입 로직 */
    public Long join(UserJoinRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // DTO 데이터를 바탕으로 User 엔티티 객체 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getNickname())
                .jobType(request.getJobType())
                .disabilityType(request.getDisabilityType())
                .cognitiveLevel(request.getCognitiveLevel())
                .build();

        return userRepository.save(user).getUserId();
    }


    /* 로그인 로직 */
    public UserLoginResponse login(String email, String password) {

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인 (차후 BCrypt 암호화 적용 예정
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 로그인 성공 시 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getEmail());
        String refreshToken = accessToken;

        return new UserLoginResponse(accessToken, refreshToken);
    }

    /* 내 정보 조회 로직 */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 엔티티를 DTO로 변환해서 반환
        return UserInfoResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .jobType(user.getJobType())
                .build();
    }

    /* 비밀번호 재설정 로직 (로그인 전 이메일 인증 후 강제 변경) */
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        // 기존 비번 확인 없이 바로 새 비번 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
    }

    /* 비밃번호 변경 로직 */
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequest request) {
        // 가입된 유저인지 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정을 찾을 수 없습니다."));

        // 기존 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        // 새 비밀번호 업데이트
        user.updatePassword(encodedPassword);
    }

    /* 소셜 유저 저장 혹은 업데이트 로직(OAuth2용) */
    public User saveOrUpdateSocialUser(String email, String name, User.RegistrationId registrationId) {
        User user = userRepository.findByEmail(email)
                .map(entity -> {
                    // 이미 있다면 이름만 업데이트 (선택 사항)
                    entity.updateName(name);
                    return entity;
                })
                .orElseGet (() -> User.builder() // 없으면 새로 생성
                        .email(email)
                        .name(name)
                        .password("SOCIAL_USER")
                        .registrationId(registrationId)
                        .build());

        return userRepository.save(user);
    }

    /* 이메일 업데이트 로직 */
    @Transactional
    public void updateEmail(String currentEmail, String newEmail) {
        // 새 이메일 중복 검사
        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 현재 유저 정보 가져오기
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이메일 변경
        user.updateEmail(newEmail);
    }

    /* 프로필 이미지 업데이트 로직 */
    @Transactional
    public void updateProfileImage(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateProfileImage(imageUrl); // 엔티티의 필드 업데이트
    }

    /* 사용자 환경 설정 업데이트 */
    @Transactional
    public void updateEnvironment(String email, ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateEnvironment(
                request.getJobType(),
                request.getDisabilityType(),
                request.getCognitiveLevel()
        );
    }

    /* 음성 재등록 로직 */
    @Transactional
    public void reRegisterVoiceFiles(String email, List<MultipartFile> voiceFiles) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 여러 파일을 저장
        // 마지막 파일의 경로를 저장하거나, 별도의 테이블이 없다면 첫 번째 경로를 대표로 저장
        String lastSavedPath = "";

        for (MultipartFile file : voiceFiles) {
            if (!file.isEmpty()) {
                lastSavedPath = fileService.saveFile(file, "voices/training");
            }
        }

        // DB 업데이트 (AI 학습용 원본 파일 경로 저장)
        user.updateVoiceFile(lastSavedPath);
    }

    /* 음성 삭제 로직 */
    @Transactional
    public void deleteVoiceFiles(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String voicePath = user.getVoiceFileUrl();

        if (voicePath != null && !voicePath.isEmpty()) {
            // 실제 물리 파일 삭제
            deletePhysicalFile(voicePath);

            // DB 정보 초기화
            user.updateVoiceFile(null);
        }
    }

    // 물리 파일 삭제를 위한 private 메서드
    private void deletePhysicalFile(String relativePath) {
        String rootPath = System.getProperty("user.dir");
        File file = new File(rootPath + relativePath.replace("/", File.separator));
        if (file.exists()) {
            file.delete(); // 파일 삭제 실행
        }
    }

    /* 음성 인식 및 말투 설정 로직 */
    @Transactional
    public void updateAiSettings(String email, AiSettingsRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateAiSettings(request.getSpeechWaitTime(), request.getPreferredTone());
    }

    /* 사전 음성 등록 로직 */
    @Transactional
    public void registerVoiceSamples(String email, List<MultipartFile> samples) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String samplePath = "";

        // 여러 개의 샘플 파일을 순회하며 저장
        for (MultipartFile file : samples) {
            if (!file.isEmpty()) {
                // 저장 폴더를 voices/samples 로 구분
                samplePath = fileService.saveFile(file, "voices/samples");
            }
        }

        // DB에 대표 경로 저장 (또는 샘플 등록 완료 처리)
        user.updateVoiceFile(samplePath);
    }

    /* 사용자 디스플레이 설정 로직*/
    @Transactional
    public void updateDisplaySettings(String email, DisplaySettingsRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateDisplaySettings(request.getFontSize(), request.getIsLargeButton());
    }

    /* 통계 데이터 조회 로직 */
    @Transactional(readOnly = true)
    public UserStatisticsResponse getStatistics(String email) {
        UserStatistics stats = statisticsRepository.findByUserEmail(email)
                .orElseGet(() -> UserStatistics.builder()
                        .totalCorrectionCount(0)
                        .averageCorrectionIntensity(0)
                        .completedRoleplays(0)
                        .generatedSummaries(0)
                        .build());

        return UserStatisticsResponse.builder()
                .totalCorrectionCount(stats.getTotalCorrectionCount())
                .averageCorrectionIntensity(stats.getAverageCorrectionIntensity())
                .completedRoleplays(stats.getCompletedRoleplays())
                .generatedSummaries(stats.getGeneratedSummaries())
                .build();
    }

    /* 로그아웃 로직 */
    public void logout(String email) {

    }

    /* 회원 탈퇴 로직 */
    @Transactional
    public void withdraw(String email, String inputPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않아 탈퇴 처리가 불가능합니다.");
        }

        // 물리 파일들 삭제 (기존 로직 동일)
        if (user.getProfileImUrl() != null) deletePhysicalFile(user.getProfileImUrl());
        if (user.getVoiceFileUrl() != null) deletePhysicalFile(user.getVoiceFileUrl());
        if (user.getVoiceSampleUrl() != null) deletePhysicalFile(user.getVoiceSampleUrl());

        // DB에서 유저 삭제
        userRepository.delete(user);
    }
}


