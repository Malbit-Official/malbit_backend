// API 응답을 일관되게 해주는 공통 응답 클래스
package com.example.demo.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({ "status", "message", "data" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final String status;
    private final String message;
    private final T data;

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    // 성공 응답 (데이터 미포함)
    public static <T> ApiResponse<Object> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>("FAIL", message, null);
    }
}