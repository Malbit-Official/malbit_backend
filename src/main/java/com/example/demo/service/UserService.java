// 회원가입 및 로그인 등 회원 관련 핵심 비즈니스 로직을 처리하는 클래스
package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.UserJoinRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;


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
                .build();

        return userRepository.save(user).getUserId();
    }


    /* 로그인 로직 */
    public LoginResponse login(String email, String password) {

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

        return new LoginResponse(accessToken, refreshToken);    }

    /* 내 정보 조회 로직 */
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 엔티티를 DTO로 변환해서 반환
        return UserResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .jobType(user.getJobType())
                .build();
    }

    /* 소셜 유저 저장 혹은 업데이트 (OAuth2용) */
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
}
