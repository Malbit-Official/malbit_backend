// 클라이언트에세 응답할 사용자 정보를 담는 데이터 전송 객체(DTO)
package com.example.demo.dto;

import com.example.demo.domain.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoResponse {
    private String email;   //  사용자 이메일
    private String name;    // 사용자 이름(닉네임)
    private JobType jobType;    // 사용자 직무
}
