package com.balnceddiet.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter     // Getter 메서드 롬복
@NoArgsConstructor  // 기본 생성자 선언하는 롬복 - 역직렬화(JSON -> Java)에 필요함
public class AnalyzeRequest {   // 브라우저가 요청하는 것에 대한 클래스
    private String text;            // 텍스트로 입력했을 때
    private String imageBase64;     // 사진으로 입력했을 때
    private String mimeType;        // 예: image/jpeg
}
