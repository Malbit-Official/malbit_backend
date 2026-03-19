// 소셜 로그인 성공 시 JWT 토큰을 생성하고,
// 프론트엔드의 특정 경로로 토큰을 담아 리다이렉트시키는 핸들러
package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 소셜 로그인 유저이 이메일 추출
        // (카카오와 구글의 이메일 위치가 다를 수 있어 CustomOAuth2UserService에서 정제한 방식을 참고)
        String email = (String) oAuth2User.getAttributes().get("email");

        // 만약 카카오라면 구조가 다름! (간단하게 email로 가정하거나 정제된 속성을 가져옴
        if (email == null) {
            // 카카오용 예시 (실제 구조에 맞춰 수정 필요)
            java.util.Map<String, Object> kakaoAccount = (java.util.Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            email = (String) kakaoAccount.get("email");
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(email);

        // 프론트에서 리다이렉트 (쿼리 피라미터에 토큰을 실어서 보냄)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
