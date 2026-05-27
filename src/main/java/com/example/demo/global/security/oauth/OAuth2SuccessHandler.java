// 소셜 로그인 성공 시 JWT 토큰을 생성하고,
// 프론트엔드의 특정 경로로 토큰을 담아 리다이렉트시키는 핸들러
package com.example.demo.global.security.oauth;

import com.example.demo.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = null;

        // 카카오 이메일 동의 거부 방어
        if (oAuth2User.getAttributes().containsKey("email")) {
            email = (String) oAuth2User.getAttributes().get("email"); // 구글
        } else if (oAuth2User.getAttributes().containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                email = (String) kakaoAccount.get("email"); // 카카오 정상 이메일
            } else {
                // 이메일이 없을 경우 카카오 고유 ID를 가져와 고유 문자열 재조립
                Object idObj = oAuth2User.getAttributes().get("id");
                email = (idObj != null ? idObj.toString() : "social_" + System.currentTimeMillis()) + "@kakao.local";
            }
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(email);

        String targetUrl = UriComponentsBuilder.fromUriString("http://3.37.239.105:8080/api/users/me")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
