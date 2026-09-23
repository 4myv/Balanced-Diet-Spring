package com.balanceddiet.server.exception;

import com.balanceddiet.server.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j  // log를 사용하여 콘솔에 기록을 남기기 위한 어노테이션
@RestControllerAdvice   // 모든 Controller를 지켜보다가, 에러가 날아오면 대신 받아줌
public class GlobalExceptionHandler {

    // 1. 사용자가 잘못 보냈을 때 -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    // @ExceptionHandler -> 괄호 안 종류의 에러가 오면 이 메서드가 받음
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
    }

    // 2. Gemini 서버에 문제가 있을 때 -> 503
    @ExceptionHandler(HttpServerErrorException.class)
    // 겪고 있는 오류를 받는다. Gemini가 5xx를 보내면
    // RestClient가 HttpServerErrorException을 던지는데, 이를 친절한 안내로 바꿈
    public ResponseEntity<ErrorResponse> handleGeminiServerError(HttpServerErrorException e) {
        log.warn("Gemini 서버 오류: {}", e.getStatusCode());
        // 콘솔에 주의 수준으로 기록 (Gemini가 바쁜 건 우리 잘못은 아니니까)
        // {}는 뒤의 값이 들어갈 자리, 뒤의 e.getStatusCode()를 {} 자리에 띄움
        // ex) Gemini 서버 오류: 503 SERVICE_UNAVAILABLE

        return ResponseEntity.status(503).body(new ErrorResponse("AI가 지금 바빠요. 잠시 후 다시 시도해주세요."));
    }
    
    // 3. 그 외 모든 에러 -> 500
    @ExceptionHandler(Exception.class)
    // Exception은 모든 에러의 부모이다
    // 위 1, 2에 해당하지 않는 에러는 모두 여기로 받아 처리한다
    public ResponseEntity<ErrorResponse> handleEtc(Exception e) {
        log.error("처리 중 에러 발생", e);
        // 콘솔에 에러 수준으로 기록, 뒤에 e를 넣어 에러 내용 전체가 콘솔에 찍힌다
        return ResponseEntity.status(500).body(new ErrorResponse("서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요."));
    }
}
