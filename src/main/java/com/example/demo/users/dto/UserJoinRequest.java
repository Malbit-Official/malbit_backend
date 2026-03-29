// JWT 로그인 DTO
package com.example.demo.users.dto;

import com.example.demo.entity.CognitiveLevel;
import com.example.demo.entity.DisabilityType;
import com.example.demo.entity.JobType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequest {
    private String email;   // 로그인에 사용할 이메일 아이디
    private String password;    // 사용자 비밀번호
    private String passwordConfirm; // 비밀번호 재확인
    private String nickname;    // 사용자 닉네임
    private JobType jobType;    // 직무 분야
    private DisabilityType disabilityType;  // 장애 유형
    private CognitiveLevel cognitiveLevel;  // 인지 수준
}
