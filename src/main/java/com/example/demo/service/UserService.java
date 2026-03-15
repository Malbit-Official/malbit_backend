// 회원가입 및 로그인 등 회원 관련 핵심 비즈니스 로직을 처리하는 클래스
package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.UserJoinRequest;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /* 회원가입 로직 */
    public Long join(UserJoinRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        // DTO 데이터를 바탕으로 User 엔티티 객체 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // 현재는 암호화 없이 저장(차후 고도화
                .name(request.getNickname())
                .jobType(request.getJobType())
                .build();
        return userRepository.save(user).getUserId();
    }

    /* 로그인 로직 */
    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
}
