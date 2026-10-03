**Spring 계층 구조 이해하기**

* **요청** -> **Controller**(받고 넘김) -> **Service**(판단ㆍ계산) -> **Repository**(저장ㆍ조회) -> **DB**



**직접 해보기 : Hello API**
```java
@RestController
// HTTP 요청을 받는 담당, "return값을 그대로 응답 본문으로 보낸다"고 알려주는 역할을 함
public class HelloController {
    
   	@GetMapping("/api/hello")   // 이 주소로 GET 요청이 오면 이 메서드를 실행
	public String hello() {
       	return "Hello, World!";
   	}
}
```



**어노테이션**

* **@Controller** **:** 리턴값을 "화면 이름"으로 해석 -> HTML 페이지를 찾음
* **@RestController :** 리턴값을 "응답 데이터"로 해석 -> JSON으로 변환
* **@GetMapping, @PostMapping, @DeleteMapping, @PutMapping** : 괄호 안 주소로 각 요청을 받았을 때 메서드를 실행
* **@RequestBody :** HTTP 요청 본문의 JSON을 Java 객체로 변환해서 받음
* **@RequestParam :** 주소의 쿼리 파라미터를 받음
* **@PathVariable :** 주소의 일부를 받음
* **@AllArgsConstructor :** 모든 필드를 파라미터로 받는 생성자
* **@NoArgsConstructor :** 기본 생성자 (파라미터 없음)



**직접 해보기 : JSON 응답**
```java
@GetMapping("/api/test")    // 이 주소로 GET 요청이 오면 이 메서드를 실행

public Map<String, Object> test() {
  // return type -> Map<String, Object> (String - JSON의 키가 될 부분, Object - 값(출력 값의 부모 클래스))

  Map<String, Object> result = new HashMap<>();	// Map 타입의 객체 선언
  // Map은 인터페이스라서 new Map()과 같이 사용할 수 없음
  // 그래서 new HashMap<>()을 사용
  // 출력했을 때, 순서가 바뀌는 이유 -> HashMap은 키의 해시값으로 위치를 정하기 때문
  // Object로 저장했기 때문에 꺼낼 때 타입캐스팅 필요

  result.put("message", "정상 동작");
  result.put("calories", 450);
  // result 객체 안에 출력할 값을 저장해둠

  return result;  // result 객체를 리턴
   }
```



**요청이 처리되는 흐름**

1. 브라우저에서 http://localhost:8080/api/test 입력
2. Tomcat이 요청을 받음
3. DispatcherServlet(스프링의 중앙 관제탑, 모든 요청이 처음 도착하는 곳)
4. "/api/test에 매핑된 메서드가 어디 있지?" 찾음
5. HelloController.test() 실행
6. Map 리턴
7. Jackson이 JSON 문자열로 변환
8. Content-Type: application/json 헤더 붙여서 응답



**Spring Boot가 대신 해주는 것**

* **내장 Tomcat**

  * Tomcat 서버를 따로 설치하고, 설정 파일 쓰고, 빌드한 파일을 올리는 과정 없이 main() 실행만으로 서버 작동
* **자동 설정 (Auto Configuration)**

  * build.gradle에 의존성만 추가하면 스프링이 알아서 필요한 설정을 해줌



**컴포넌트 스캔과 빈(Bean)**

* **빈이란?**

  * 스프링이 직접 만들어서 관리하는 객체, new로 생성할 필요 없음
* **스캔 범위**

  * **@SpringBootApplication**이 붙은 클래스가 있는 패키지와 그 하위만 스캔
  * **스캔 대상 -> @Component** / @RestController, @Controller, @Service, @Repository





**HTTP 메서드**

|메서드|의미|
|-|-|
|POST|생성|
|GET|조회|
|PUT/PATCH|수정|
|DELETE|삭제|

**--** GET과 POST의 실질적 차이

* GET : 데이터를 **주소에 담아** 보냄 -- 길이 제한 있음, 주소창에 노출
* POST : 데이터를 **본문(body)에 담아** 보냄 -- 길이 제한 없음, 이미지같은 큰 데이터도 가능



**직렬화(Serialization)**

* **Java** 객체 -> **JSON** 문자열 (응답 보낼 때)
* 역직렬화 : **JSON** 문자열 ->  **Java** 객체 (요청 받을 때) -- **기본 생성자 필요**
* **getter**를 기준으로 동작

```java
public class TestResponse {
	private String message;   // getter 없음
}
// 결과: {} ← 빈 JSON
```
~getter 유/무~
```java
@Getter                       // Lombok이 getter 생성
public class TestResponse {
   		private String message;
}
// 결과: {"message":"..."}
```



**Lombok**

* 어노테이션으로 반복 코드를 자동 생성해주는 라이브러리

|**어노테이션**|**생성되는 것**|
|-|-|
|@Getter/@setter|모든 필드의 getter/setter|
|@AllArgsConstructor|모든 필드를 받는 생성자|
|@NoArgsConstructor|파라미터 없는 기본 생성자|
|@Data|위 대부분을 한 번에|



**DTO를 쓰는 이유**

* **DTO란?** -- 데이터를 옮기는 용도의 객체
* **Map** vs **DTO**

||**MAP**|**DTO**|
|-|-|-|
|**오타**|런타임에 발견|**컴파일 에러**로 즉시|
|**타입**|전부 **Object**, 타입 캐스팅 필요|명확|
|**구조 파악**|코드 다 읽어야 함|클래스만 보면 됨|
|**JSON 순서**|HashMap은 보장 안 됨|필드 선언 순서|



