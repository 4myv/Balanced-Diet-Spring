package com.balnceddiet.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
// HTTP 요청을 받는 담당, "return값을 그대로 응답 본문으로 보낸다"고 알려주는 역할을 함
public class HelloController {

    @GetMapping("/api/hello")   // 이 주소로 GET 요청이 오면 이 메서드를 실행
    public String hello() {
        return "Hello, World!";
    }
    
    @GetMapping("/api/test")    // 이 주소로 GET 요청이 오면 이 메서드를 실행
    public Map<String, Object> test() {
        // return type -> Map<String, Object> (String - JSON의 키가 될 부분, Object - 값(출력 값의 부모 클래스))

        Map<String, Object> result = new HashMap<>();   // Map 타입의 객체 선언
        // Map은 인터페이스라서 new Map()과 같이 사용할 수 없음
        // 그래서 new HashMap<>()을 사용
        // 출력했을 때, 순서가 바뀌는 이유 -> HashMap은 키의 해시값으로 위치를 정하기 때문
        // Object로 저장했기 때문에 꺼낼 때 타입캐스팅 필요

        result.put("message", "정상 동작");
        result.put("calories", 450);
        // result 객체 안에 출력할 값을 저장해둠

        return result;  // result 객체를 리턴
    }
}
