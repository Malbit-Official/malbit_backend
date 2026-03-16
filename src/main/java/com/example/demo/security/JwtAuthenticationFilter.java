// 모든 HTTP 요청에서 JWT 토큰을 추출하여 유효성을 검증하고,
// 인증된 사용자의 정보를 보안 컨텍스트에 등록하는 보안 필터
package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 토큰이 존재하고 유효하면 인증 처리 수행
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getUserEmail(token);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, java.util.Collections.emptyList());

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출
    // 형식: Authorization: Bearer {JWT_TOKEN}
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
