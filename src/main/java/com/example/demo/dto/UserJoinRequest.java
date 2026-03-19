// 회원가입 시 프론트로부터 전달받는 데이터를 담는 바구니(DTO)
package com.example.demo.dto;

import com.example.demo.domain.JobType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequest {
    private String email;   // 로그인에 사용할 이메일 아이디
    private String password;    // 사용자 비밀번호
    private String passwordConfirm; // 비밀번호 재확인
    private String nickname;    // 사용자 닉네임(DB의 name 칼럼과 매핑)
    private JobType jobType;    // 직무 분야
}
