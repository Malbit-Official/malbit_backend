// 소셜 로그인 비즈니스 로직
package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    public String login(String provider, String token) {
        String email = "";

        if ("kakao".equals(provider)) {
            email = getEmailFromKakao(token);
        } else if ("google".equals(provider)) {
            email = getEmailFromGoogle(token);
        }

        // DB 확인 및 자동 가입
        String finalEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(finalEmail)
                        .name("Social User")
                        .password("")
                        .registrationId(User.RegistrationId.valueOf(provider.toUpperCase()))
                        .build()));

        return jwtTokenProvider.createToken(user.getEmail());
    }

    // 카카오 서버에 이메일 물어보는 메서드
    private String getEmailFromKakao(String token) {

        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = response.getBody();
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");

        return (String) kakaoAccount.get("email");

    }

    // 구글 서버에 이메일 물어보는 메서드
    private String getEmailFromGoogle(String token) {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return (String) response.getBody().get("email");
    }
}
