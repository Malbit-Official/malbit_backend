//  소셜 로그인(OAuth2) 성공 시 가져온 유저 정보를 바탕으로
// 우리 서비스의 유저 데이터를 생성하거나 업데이트하는 비지니스 로직
package com.example.demo.global.security.oauth;

import com.example.demo.entity.User;
import com.example.demo.users.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 어떤 서비스(카카오, 구글)인지 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 유저 정보 키값 추출 (구굴은 'sub', 카카오는 'id')
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 실제 유저 데이터(이메일, 이름 등) 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오/구글 마다 데이터 구조가 달라서 정제 과정 필요
        String email = "";
        String name = "";

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            
            // 이메일 추출 방어 로직 안전화
            if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                email = (String) kakaoAccount.get("email");
            } else {
                Object idObj = attributes.get("id");
                email = (idObj != null ? idObj.toString() : "social_" + System.currentTimeMillis()) + "@kakao.local";
            }

            // 이름/닉네임 추출 안전화 
            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                
                if (profile != null && profile.get("nickname") != null) {
                    name = (String) profile.get("nickname");
                } else if (kakaoAccount.get("name") != null) {
                    // 카카오 비즈니스 채널 설정에 따라 진짜 이름이 넘어오는 경우 대비
                    name = (String) kakaoAccount.get("name"); 
                } else {
                    name = "Kakao User";
                }
            } else {
                name = "Kakao User";
            }
            
        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            if (name == null)
                name = "Google User";
        }

        // DB에 저장하거나 업데이트
        User.RegistrationId socialType = User.RegistrationId.valueOf(registrationId.toUpperCase());
        userService.saveOrUpdateSocialUser(email, name, socialType);

        System.out.println("OAUTH_DEBUG: email = " + email);
        System.out.println("OAUTH_DEBUG: name = " + name);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName);
    }
}
