package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserSocialResponse {

    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;
    private String message;
}
