package com.balanceddiet.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter    // 직렬화에 사용됨

@AllArgsConstructor
// 모든 파라미터를 받는 생성자 롬복
// 여기서는 new ErrorResponse("메시지")로 만들기 위해 붙임

public class ErrorResponse {
    private String message;
}
