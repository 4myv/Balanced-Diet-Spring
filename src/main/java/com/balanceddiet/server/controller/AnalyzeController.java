package com.balanceddiet.server.controller;

import com.balanceddiet.server.dto.AnalyzeRequest;
import com.balanceddiet.server.dto.AnalyzeResponse;
import com.balanceddiet.server.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
// 이 클래스 안 모든 주소 앞에 /api를 붙인다
public class AnalyzeController {

    private final GeminiService geminiService;

    // 생성자
    public AnalyzeController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze") // POST 요청이 오면 이 메서드를 실행
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
        // @RequestBody -> 클라이언트가 HTTP Body에 담아 보낸 데이터를 자바 객체로 변환해 줌
        // 사용자가 보낸 JSON을 AnalyzeRequest 객체로 바꿔서 request에 담음
        // ex) Body -> { "text" : "kimchi stew 1 serving" }
        // "text"가 이름이 같은 AnalyzeRequest의 text 필드로 들어감

        return geminiService.analyzeMeal(request);
        // 클라이언트가 보낸 데이터가 담긴 request 객체를 파라미터로 담아서
        // GeminiService의 analyzeMeal() 메서드 실행한 값을 리턴함
        // @RestController 어노테이션 때문에 리턴 값이 페이지에 표시됨
    }
}