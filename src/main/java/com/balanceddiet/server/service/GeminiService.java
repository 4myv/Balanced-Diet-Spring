package com.balanceddiet.server.service;

import com.balanceddiet.server.dto.AnalyzeRequest;
import com.balanceddiet.server.dto.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String MEAL_PROMPT =   // 상수 선언
            "너는 영양 분석가야. 사용자가 보낸 음식 사진이나 설명을 보고 예상 영양 성분을 추정해. " +
            "다른 설명 없이 아래 형식의 JSON 객체 하나만 출력해: " +
            "{\"foodName\": \"짧은 요약 이름\", \"calories\": 정수, \"carb\": 정수, \"protein\": 정수, \"fat\": 정수}";
    // \" -> JSON 예시 속 따옴표를 넣기 위한 이스케이프

    // 서비스 클래스에는 "요청마다 달라지는 값"을 필드로 두지 않는다
    // 빈은 기본적으로 하나가 만들어져서 모든 요청을 같이 사용
    // GeminiService라는 한 객체 안의 값이 요청 도중에 바뀌면 다른 사용자의 요청과 섞일 수 있음
    // 그래서 필드를 final로 선언
    private final RestClient restClient;
    // RestClient: 우리 서버가 Gemini에 요청 보낼 때 쓰는 fetch
    private final ObjectMapper objectMapper;
    // ObjectMapper: JSON과 Java 객체를 서로 바꿔주는 변환기
    private final String apiKey;
    private final String model;

    public GeminiService(ObjectMapper objectMapper,     // 스프링이 채워줌
                         // @Value를 사용하여 빈 말고 설정 파일 값에서 가져오라 함
                         @Value("${gemini.api-key}") String apiKey,
                         @Value("${gemini.model}") String model) {
        this.restClient = RestClient.create("https://generativelanguage.googleapis.com");
        // create("base URL") -- 기본 주소 지정
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AnalyzeResponse analyzeMeal(AnalyzeRequest request) {
        // 분석 요청 메소드

        // 이미지 요청과 텍스트 요청을 구별하기
        boolean hasImage = request.getImageBase64() != null && !request.getImageBase64().isBlank();
        boolean hasText = request.getText() != null && !request.getText().isBlank();

        if (!hasImage && !hasText) {    // 둘다 없으면 실행
            throw new IllegalArgumentException("텍스트나 사진 중 하나는 필요합니다");
            // throw -> 개발자의 의도와 다르게 진행될 때,
            // Java가 직접 설명할 수 없는 오류 상황에서 개발자가 던지는 에러
            // (던진다)throw (만든다)new (에러 종류)IllegalArgumentException("에러 설명")
        }

        // 1. parts 만들기 (사진 우선)
        List<Map<String, Object>> parts = new ArrayList<>();
        if (hasImage) {     // 사진이 있으면 실행
            String mimeType = request.getMimeType() != null ? request.getMimeType() : "image/jpeg";
            // 브라우저가 보낸 값이 있으면 그걸 쓰고, 없으면 기본값

            parts.add(Map.of("inline_data", Map.of("mime_type", mimeType, "data", request.getImageBase64())));
            // Map이 들어있는 Map을 리스트에 추가
            // { "inline_data" : {"mime_type" : image/jpeg, "data" : "Base64로 변환한 긴 문자열" } }
            parts.add(Map.of("text", "이 사진 속 음식의 영양 성분을 분석해줘."));
        } else {    // 텍스트 요청을 받은 경우
            parts.add(Map.of("text", request.getText()));
            // ex) { "text" : "김치찌개 1인분, 공기밥 한 그릇" }
        }

        // 2. 요청 본문 만들기
        // Gemini에게 보내는 양식 { systemInstruction, contents, generationConfig }
        Map<String, Object> body = Map.of(
                // systemInstruction -> AI에게 주는 역할과 규칙
                // (너는 영양 분석가다, JSON으로만 답해라)
                "systemInstruction", Map.of("parts", List.of(
                        Map.of("text", MEAL_PROMPT)
                )),
                // { "systemInstruction" : { "parts" :
                // List [ { "text" : MEAL_PROMPT } ] }

                // contents -> 실제 질문
                // (이 사진/텍스트를 분석해줘)
                "contents", List.of(Map.of("parts", parts)),
                // { contents : List [ { "parts" : parts } ] }

                // generationConfig -> 답변 방식 설정
                // (답은 JSON 형식으로)
                "generationConfig", Map.of("responseMimeType", "application/json")
                // { generationConfig : { "responseMimeType" : "application/json" } }
        );

        // 3. Gemini에게 보내기
        Map<String, Object> response = restClient.post()    // POST 방식으로
                .uri("/v1beta/models/{model}:generateContent", model)
                // 보낼 주소의 나머지 부분 {model}에 변수 model이 들어감
                .header("x-goog-api-key", apiKey)
                // API 키를 헤더에 담음
                .contentType(MediaType.APPLICATION_JSON)
                // 보내는 타입이 JSON이라고 알려줌
                .body(body)
                // body를 실어 보냄 (직렬화를 통해 Map을 JSON으로 보냄_
                .retrieve()
                // 요청을 보내고 응답을 받음
                .body(Map.class);
                // 받은 답을 Map으로 바꿔 달라 (역직렬화)

        // 4. 응답에서 텍스트만 꺼내서 DTO로 변환
        // response 구조
        // response : (Object)
        // "candidates" [       -- candidates란? Gemini가 응답할 때 붙이는 이름표
        // List [ { "content" : { "parts" :
        // List [ { "text" : "{\"foodName\":\"김치찌개\",...}" } ] } } ] ]
        // 우리에겐 "text"만 필요!!

        String text = extractText(response);
        // extractText() 메서드에 파라미터 response를 넣어 호출 -> text 변수에 저장

        String cleaned = text.replace("```json","").replace("```", "").trim();
        // replace( target, replacement ) -> target: 찾을값, replacement: 바꿀값
        // trim() -> 앞뒤 공백 지우기
        // Gemini가 ```json ... ``` 으로 감싸서 주면, 벗겨내서 cleaned 변수에 저장

        // try/catch문 -- 엑셀의 IfError 함수 "실패하면 이렇게 해라"
        try {
            return objectMapper.readValue(cleaned, AnalyzeResponse.class);
            // readValue -> 앞의 값을 뒤의 값으로 바꿈
            // JSON 글자를 AnalyzeResponse 객체로 바꿔서 리턴함
        } catch (Exception e) {
            // try가 실패할 시
            throw new RuntimeException("AI 응답을 해석하지 못했습니다: " + cleaned, e);
            // AI 응답을 해석하지 못했습니다 : AI 응답
        }
    }

    private String extractText(Map<String, Object> response) {
        // Map<String, Object> 로 선언 -> "이름표"를 통해 Object를 호출
        // Map에서 get("이름표")로 꺼낼 때 -> 값이 Object라서 캐스팅 필요
        // List<Map<String, Object>> 로 선언 -> 인덱스를 통해 Map을 호츨
        // List에서 get(번호)로 꺼낼 때 -> 값이 Map이라서 캐스팅 필요 없음

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        // Object를 담은 "response"에서 List<Map<String, Object>>로 타입 캐스팅

        Map<String, Object> firstCandidate = candidates.get(0);
        // Map을 담은 "candidates"에서 캐스팅 없이 인덱스로 꺼냄

        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        // Object를 담은 "firstCandidate"에서 Map<String, Object>로 타입 캐스팅

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        // Object를 담은 "content"에서 List<Map<String, Object>>로 타입 캐스팅

        Map<String, Object> firstpart = parts.get(0);
        // Map을 담은 "parts"에서 캐스팅 없이 인덱스로 꺼냄

        return (String) firstpart.get("text");
        // Object를 담은 "firstpart"에서 이름이 "text"에 해당하는 것을
        // String으로 타입 캐스팅해서 리턴함
    }
}
