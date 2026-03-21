// JWT(JSON Web Token)를 생성하고 유효성을 검증하며, 토큰에서 사용자 정보를 추출
package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

// 사용자 인증을 위한 JWT 토큰을 생성, 복호화 및 유효성을 검증하는 컴포넌트
@Component
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.expiration-time}")
    private long expirationTime;

    private Key key;

    @PostConstruct
    protected void init() {
        // 비밀키를 암호화 알고리즘에 맞게 변환
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email); // 토큰에 담을 정보 (Payload)
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime); // 만료 시간 계산

        return Jwts.builder()
                .setClaims(claims)  // 정보 저장
                .setIssuedAt(now)   // 토큰 발행 시간 정보
                .setExpiration(validity)    // 토큰 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)    // 암호화 알고리즘과 키 설정
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try{
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e){
            return false;
        }
    }

    // 토큰에서 유저 이메일 추출
    public String getUserEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
