**이번 단계의 목표**

```
[기존]  브라우저(script.js에 API 키) ──────────────→ Gemini
[변경]  브라우저 ──→ 내 Spring 서버(API 키 보관) ──→ Gemini
```

**IntelliJ에 환경변수 등록**

Run -> Edit Configurations -> ServerApplication -> Environment variables

\-> GEMINI\_API\_KEY=키



**설정값 - @Value와 환경변수**

```properties
gemini.api-key=${GEMINI_API_KEY}
# "환경변수에서 GEMINI_API_KEY 이름의 값을 가져와라"
gemini.model=gemini-3.5-flash-lite
```

* @Value -> 빈 말고 설정 파일에서 값을 가져오라고 함
* 모델 이름과 같이 바뀔 수 있는 값은 설정으로 빼두면 코드 수정 없이 교체 가능



**의존성 주입 (DI)**

* 의존성 - 필요한 다른 객체
* 주입 - 밖에서 넣어줌
* new를 사용한 인스턴스화 과정이 필요 없음
* 생성자에 필요한 객체만 적어두면, 스프링은 객체를 한 번만 만들어서
필요로 하는 곳 모두에 같은 객체를 넣어줌 -> 여러 사용자의 요청을 같은 객체 하나가 처리함
* 그래서 모두에게 같은 값(API 키 등)은 final 필드에, 요청마다 달라지는 값은 메서드 안 지역변수에 둠
-> 지역변수는 요청마다 새로 만들어지므로 사용자끼리 값이 섞이지 않음



**RestClient - 우리 서버가 다른 서버에 요청**

```java
restClient.post()
    .uri(...)                           // 주소
    .header("x-goog-api-key", apiKey)   // 누가 보냈는지
    .contentType(APPLICATION_JSON)      // 내용물 종류
    .body(body)                         // 보낼 내용 (Map → JSON 자동 변환)
    .retrieve()                         // 전송 + 응답 받기 (여기서 실제 통신)
    .body(Map.class);                   // 받은 JSON → Map
```


**ObjectMapper** **- JSON 변환기**

* Gemini의 응답이 JSON이라서 JSON 안쪽 데이터를 리턴 형식에 맞게 readValue()를 통해 변환할 때 사용했음



**ResponseEntity - HTTP 응답에 필요한 핵심 요소 제어ㆍ저장**

* 상태 코드 (Status Code) : 400 Bad Request, 404 Not Found 등 HTTP 상태 코드 지정

  * 4xx -> 클라이언트 잘못 (400 잘못된 요청, 404 주소를 찾을 수 없음)
  * 5xx -> 서버 쪽 문제 (500 내부 오류, 503 일시 불가)
* Header : 응답 헤더에 메타데이터를 추가할 수 있음
* Body : 클라이언트에 실제로 전송할 데이터를 담아둠



**어노테이션**

* **@RestControllerAdvice** : 모든 Controller의 예외 처리(에러)를 한 곳에서 받음
* **@ExceptionHandler** : 괄호 안 종류의 에러가 오면 이 메서드가 받음
* **@Slf4j** : log를 사용할 수 있게 해줌
* **@Autowired** : 의존성을 주입해줌 (생성자가 하나뿐이면 생략하는게 일반적)



**Map**

||**ArrayList**|**HashMap**|
|-|-|-|
|**찾는 기준**|get(번호)|get("이름표")|
|**넣기**|add(값)|put("이름표", 값)|

* 같은 이름표에 put -> 덮어씀
* 없는 이름표를 get으로 찾을 경우 -> null 반환
* Map.of()는 만든 뒤 수정할 수 없음
* Map.of()는 값에 null을 넣을 수 없고, get(null)도 에러가 발생함 -> 그래서 값 검사를 먼저 함



**형변환 (타입캐스팅) 여부**

* Map<String, Object>에서 꺼내려면 Object라서 형변환이 필요함 -> get("이름표")
* List<Map< \~, \~>>에서 꺼낼 때는 선언할 때 형태를 지정했기 때문에 형변환이 필요 없음-> get(인덱스 번호)



**throw / try-catch**

* throw -> throw new 에러종류("설명")
* try -> 에러 발생 가능한 코드 작성
* catch -> 에러 발생 시 실행할 코드 작성
* try 안에서 에러가 나면 즉시 catch로 -> try의 나머지 줄은 건너뜀
* catch에서 아무것도 안 하면 에러가 없던 일이 됨 -> 그래서 설명을 붙여 다시 던짐
* 같이 사용하는 예시 -  try { 에러 코드} catch { throw new 에러종류("설명") }



**설계 원칙**

* 프록시는 받은 걸 그대로 전달하면 안 된다 -> 프롬프트는 서버가 갖고, 브라우저는 재료만 보냄
* 서버는 브라우저를 믿지 않는다 -> 브라우저에서 검사해도 서버에서 한 번 더 검사함
* 같은 건 메서드 하나로, 매개변수로 기능을 구별



**Git Commit Message**

|**타입**|**언제**|
|-|-|
|feat|새 기능|
|fix|버그 수정|
|refactor|동작은 그대로, 구조만 개선|
|docs|문서|
|chore|설정, 삭제 등|



