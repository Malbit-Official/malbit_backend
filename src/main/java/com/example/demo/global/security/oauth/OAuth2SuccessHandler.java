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

        // 만약 Attributes에 직접 접근한다면 안전한 추출 로직:
        if (oAuth2User.getAttributes().containsKey("email")) {
            email = (String) oAuth2User.getAttributes().get("email"); // 구글 등
        } else if (oAuth2User.getAttributes().containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            email = (String) kakaoAccount.get("email"); // 카카오
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(email);

        // 프론트에서 리다이렉트 (쿼리 피라미터에 토큰을 실어서 보냄)
        // String targetUrl =
        // UriComponentsBuilder.fromUriString("http://localhost:8080/api/users/me")
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/api/users/me")
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
