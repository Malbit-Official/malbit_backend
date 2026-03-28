// 내 정보 조회 DTO
package com.example.demo.users.dto;

import com.example.demo.entity.JobType;
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
