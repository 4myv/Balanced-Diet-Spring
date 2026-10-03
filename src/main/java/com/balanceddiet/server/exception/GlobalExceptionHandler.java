package com.balanceddiet.server.exception;

import com.balanceddiet.server.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j  // log를 사용하여 콘솔에 기록을 남기기 위한 어노테이션
@RestControllerAdvice   // 모든 Controller를 지켜보다가, 에러가 날아오면 대신 받아줌
public class GlobalExceptionHandler {

    // 1. 사용자가 잘못 보냈을 때 -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    // @ExceptionHandler -> 괄호 안 종류의 에러가 오면 이 메서드가 받음
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        // 400 상태 코드를 알려주고, 안내 메시지 보내기
    }

    // 2. 요청 형식이 잘못됐을 때 -> 400
    @ExceptionHandler({     // 중괄호로 묶음으로써 여러 종류를 한 메서드가 받는다.
            HttpMessageNotReadableException.class,                 // JSON 모양이 깨졌거나 자료형이 안 맞음
            MissingServletRequestParameterException.class,  // ?date= 같은 필수 값이 빠짐
            MethodArgumentTypeMismatchException.class       // ?date=어제 처럼 바꿀 수 없는 값
    })
    public ResponseEntity<ErrorResponse> handleBadFormat(Exception e) {
        log.warn("잘못된 요청 형식: {}", e.getMessage());      // 개발용 기록
        return ResponseEntity.status(400).body(new ErrorResponse("요청 형식이 올바르지 않아요. 보낸 값을 확인해주세요."));
        // 400 상태 코드를 알려주고, 안내 메시지 보내기
    }

    // 3. Gemini 서버에 문제가 있을 때 -> 503
    @ExceptionHandler(HttpServerErrorException.class)
    // Gemini 서버 오류(5xx)를 받는다
    // RestClient가 HttpServerErrorException을 던지는데, 이를 친절한 안내로 바꿈
    public ResponseEntity<ErrorResponse> handleGeminiServerError(HttpServerErrorException e) {
        log.warn("Gemini 서버 오류: {}", e.getStatusCode());
        // 콘솔에 주의 수준으로 기록 (Gemini가 바쁜 건 서버 잘못은 아니니까)
        // {}는 뒤의 값이 들어갈 자리, 뒤의 e.getStatusCode()를 {} 자리에 띄움
        // ex) Gemini 서버 오류: 503 SERVICE_UNAVAILABLE

        return ResponseEntity.status(503).body(new ErrorResponse("AI가 지금 바빠요. 잠시 후 다시 시도해주세요."));
        // 503 상태 코드를 알려주고, 안내 메시지 보내기
    }

    // 4. Gemini가 4xx를 보냈을 때 -> 503 (사용량 초과) / 500 (설정 문제)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleGeminiClientError(HttpClientErrorException e) {
        if (e.getStatusCode().value() == 429) {
            log.warn("Gemini 사용량 초과 : {}", e.getStatusCode());
            return ResponseEntity.status(503).body(new ErrorResponse("AI 요청이 몰려서 잠시 쉬고 있어요. 몇 분 뒤에 다시 시도해주세요."));
            // 503 상태 코드를 알려주고, 안내 메시지 보내기
        }

        log.error("Gemini 요청 오류: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        // Gemini가 응답한 오류 전체를 Error로 로그에 남긴다

        return ResponseEntity.status(500).body(new ErrorResponse("AI 연결 설정에 문제가 있어요. 잠시 후 다시 시도해주세요."));
        // 500 상태 코드를 알려주고, 안내 메시지 보내기
    }

    // 5. 없는 주소나 파일을 요청했을 때 -> 404
    @ExceptionHandler(NoResourceFoundException.class)
    // Not Found 에러가 왔을 때 메서드 실행 (주소나 파일이 존재하지 않을 때)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException e) {
        // 404 상태 코드를 알려주고, 안내 메시지 보내기
        return ResponseEntity.status(404).body(new ErrorResponse("요청한 주소를 찾을 수 없어요."));
    }

    // 6. 찾는 데이터가 없을 때 -> 404
    @ExceptionHandler(NotFoundException.class)
    // Not Found 에러가 왔을 때 메서드 실행 (데이터가 존재하지 않을 때)
    public ResponseEntity<ErrorResponse> handleDataNotFound(NotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        // 404 상태 코드를 알려주고 안내 메시지 보내기
    }

    // 7. 그 외 모든 에러 -> 500
    @ExceptionHandler(Exception.class)
    // Exception은 모든 에러의 부모이다
    // 위 1~6에 해당하지 않는 에러는 모두 여기로 받아 처리한다
    public ResponseEntity<ErrorResponse> handleEtc(Exception e) {
        log.error("처리 중 에러 발생", e);
        // 콘솔에 에러 수준으로 기록, 뒤에 e를 넣어 에러 내용 전체가 콘솔에 찍힌다
        return ResponseEntity.status(500).body(new ErrorResponse("서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요."));
    }
}
