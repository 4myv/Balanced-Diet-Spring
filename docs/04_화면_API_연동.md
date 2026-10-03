**방향을 바꾼 이유**
- Mustache로 서버 렌더링을 시도했다가, 백엔드 직무에 맞게 "API + 기존 화면 연동"으로 전환
- 이유: 실무에서 백엔드는 주로 API를 만들고 화면은 분리. 이미 만든 API로 화면을 붙이면 서버 코드 수정 없이 완성 가능

**서버가 화면을 내보내는 방식**

||**서버가 보내는 것**|**쓰는 곳**|
|-|-|-|
|```@RestController```|데이터(JSON)|/api/...|
|```@Controller```(Mustache)|완성된 HTML-서버가 빈칸을 채움|-|
|```static/```디렉터리|파일 그대로|index.html, CSS, JS, 이미지|
- ```static/index.html```은 스프링이 / 주소의 첫 화면으로 사용
- ```static/css/style.css``` -> ```/css/style.css```처럼 폴더 주소가 실제 주소가 됨

**폼과 JSON-보내는 형식이 다르다**

||**모양**|**받는 어노테이션**|**채워주는 도구**|**setter 유/무**|
|-|-|-|-|-|
|**HTML폼**|```height=176&weight=70```|```@ModelAttribute```|스프링|필요|
|**JSON (fetch, curl)**|```{"height":176,...}```|```@RequestBody```|Jackson|불필요
- 요청을 못 받는 버그의 흔한 원인 -> 보내는 형식에 적합하지 않은 어노테이션 사용

**redirect와 PRG** <br>
```POST(저장) -> 서버가 302 "이 주소로 가라" -> 브라우저가 GET으로 새로 요청```
- 저장(POST) 뒤에는 redirect - 새로고침으로 같은 요청이 다시 가서 두 번 저장되는 것을 막음
- PRG패턴 (Post -> Redirect -> Get)
- 계산해서 보여주기만 하는 요청은 두 번 가도 데이터가 바뀌지 않음 -> redirect 불필요

**브라우저에서 API 부르기 - fetch**
RestClient와 유사함

|**RestClient (Java)**|**fetch (JS)**|
|-|-|
|```.post()```|```method: "POST"```|
|```.contentType(JSON)```|```headers: {"Content-Type": "application/json"```|
|```.body(body)```-Jackson이 변환|```body: JSON.stringify(body)```|
|```.retrieve()```|```await fetch(...)```|
- 화면이 보내는 JSON의 이름표 = 서버 DTO의 필드 이름

**같은 서버에서 화면을 주면 CORS 문제가 없다**
- CORS: 브라우저는 화면을 준 곳과 다른 주소(다른 도메인ㆍ포트)로 API를 부르는 걸 기본적으로 막음
- 현재는 화면(```static/```)과 API(```/api/...```)가 같은 서버라서 문제 없음
- 화면과 서버를 따로 배포하면 서버에서 허용 설정이 필요함

**연동하면서 발견한 문제**

|**문제**|**해결**|**교훈**|
|-|-|-|
|1차 JS가 날짜를 세계 표준시로 구함 -> 한국 오전 9시 전에는 어제 날짜|브라우저 현지 날짜로|시간대는 항상 명시. 원칙적으로 날짜 판단은 서버가|
|사용자 입력(음식 이름)을 HTML로 끼워 넣음|글자 그대로 (textContent) 넣기|입력값을 HTML로 해석하면 화면을 망가뜨리는 코드가 들어갈 수 있음|
|사진 원본을 그대로 보내면 데이터 크기가 큼|보내기 전 1024px로 맞춤|요청 크기 = 속도 + 외부 API 사용량|
|1차 script.js에 이전 API 키 노출|코드에서 API 키 삭제 -> 환경변수 설정|비밀값 확인 습관|