# Balanced Diet

사진 한 장으로 식단을 기록하면 AI가 음식과 영양 성분을 분석해주는 **일일 식단 관리 서비스**의 백엔드입니다.

신체 정보를 입력하면 AI가 하루 목표 칼로리와 탄수화물·단백질·지방을 계산하고, 먹은 음식을 글이나 사진으로 등록하면 영양 성분을 추정해 오늘의 섭취 현황과 주간 평균을 보여줍니다.

> 1차 버전은 서버 없이 브라우저에서 AI API를 직접 호출하는 구조였습니다. API 키 노출과 데이터 보관 문제를 해결하기 위해 Spring Boot 서버로 전환한 것이 이 저장소입니다.

<br>

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4, Spring Web, Spring Data JPA |
| Database | H2 (파일 모드) |
| AI | Google Gemini API (`RestClient`로 서버에서 호출) |
| Build | Gradle |
| Etc | Lombok |
| Frontend | HTML, CSS, JavaScript (`static/`에서 제공) |

<br>

## 구조

```
브라우저 (static/index.html)
   │  fetch (JSON)
   ▼
Controller  →  Service  →  Repository  →  H2
                  │
                  └──→ GeminiService  →  Gemini API
```

- **API 키는 서버에만** 존재합니다. 환경변수로 주입되며 코드와 저장소에 포함되지 않습니다.
- **프롬프트도 서버가 보관**합니다. 브라우저는 분석할 재료(텍스트·사진)만 보내므로, 서버가 범용 AI 창구로 악용되지 않습니다.
- 엔티티를 외부에 직접 노출하지 않고, 요청·응답 DTO를 용도별로 분리했습니다.

```
src/main/java/com/balanceddiet/server
├── controller   요청을 받아 Service에 전달하고 DTO로 응답
├── service      값 검사와 비즈니스 규칙, Gemini 호출
├── domain       엔티티(Meal, Profile)와 Repository
├── dto          요청·응답 객체
└── exception    전역 예외 처리
```

<br>

## API

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/profile` | 신체 정보·목표 조회 (없으면 `404`) |
| `POST` | `/api/profile` | 신체 정보 저장 (없으면 생성, 있으면 수정) |
| `POST` | `/api/goal/calculate` | 저장된 신체 정보와 목표 종류로 AI 목표 계산 |
| `POST` | `/api/profile/goal` | 목표 저장 |
| `POST` | `/api/analyze` | 식단 분석 — 텍스트 또는 사진(base64) |
| `POST` | `/api/meals` | 식단 기록 저장 |
| `GET` | `/api/meals?date=YYYY-MM-DD` | 날짜별 식단 조회 |
| `DELETE` | `/api/meals/{id}` | 식단 기록 삭제 |
| `GET` | `/api/meals/weekly-average` | 최근 7일 중 기록한 날 기준 하루 평균 |

**에러 응답**은 상황별 상태 코드와 함께 아래 형식으로 반환합니다.

```json
{ "message": "신체 정보를 먼저 입력해주세요" }
```

| 상태 코드 | 상황 |
|---|---|
| `400` | 잘못된 요청 값 |
| `404` | 없는 주소·자원 |
| `503` | Gemini 서버 일시 장애 |
| `500` | 그 외 서버 오류 |

<details>
<summary>요청·응답 예시</summary>

**목표 계산** `POST /api/goal/calculate`

```json
// 요청
{ "goalType": "bulk" }

// 응답
{ "calories": 2800, "carb": 350, "protein": 140, "fat": 80 }
```

**식단 분석 (텍스트)** `POST /api/analyze`

```json
// 요청
{ "text": "김치찌개 1인분, 공기밥 한 그릇" }

// 응답
{ "foodName": "김치찌개와 공기밥", "calories": 650, "carb": 95, "protein": 25, "fat": 18 }
```

**식단 분석 (사진)** `POST /api/analyze`

```json
{ "imageBase64": "/9j/4AAQ...", "mimeType": "image/jpeg" }
```

`goalType`은 `bulk`, `diet`, `health`, `custom` 중 하나이며, `custom`일 때는 `customGoalText`가 필요합니다.

</details>

<br>

## 실행 방법

### 1. 저장소 받기

```bash
git clone https://github.com/4myv/Balanced-Diet-Spring.git
cd Balanced-Diet-Spring
```

### 2. Gemini API 키 준비

[Google AI Studio](https://aistudio.google.com/apikey)에서 API 키를 발급받아 **환경변수 `GEMINI_API_KEY`**로 설정합니다.

- **IntelliJ** — Run → Edit Configurations → `ServerApplication` → Modify options → Environment variables → `GEMINI_API_KEY=발급받은키`
- **터미널 (Windows)** — `set GEMINI_API_KEY=발급받은키`

`application.properties`에는 키 대신 `${GEMINI_API_KEY}`만 들어 있습니다.

### 3. 실행

```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

`http://localhost:9090`에 접속합니다.

> Gemini 모델 이름은 `application.properties`의 `gemini.model`에서 바꿀 수 있습니다. 모델이 종료되거나 과부하일 때 코드 수정 없이 교체할 수 있습니다.

### 개발용 DB 확인

`http://localhost:9090/h2-console`

| 항목 | 값 |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/balanced-diet` |
| User Name | `sa` |
| Password | (비움) |

DB 파일은 `data/`에 생성되며 저장소에는 포함되지 않습니다. H2 콘솔은 개발용이며 배포 시에는 비활성화해야 합니다.

<br>

## 주요 구현

**사진 분석** — AI 요청은 JSON(텍스트) 형식이라 이미지 파일을 그대로 담을 수 없어, base64 문자열로 변환해 질문 문장과 같은 요청에 함께 담아 보냅니다.

**Gemini 호출 공통화** — 식단 분석과 목표 계산은 프롬프트와 질문만 다르고 호출 과정은 같아, `callGemini(systemPrompt, parts)`로 공통 부분을 분리했습니다.

**한 행만 유지하는 신체 정보** — 사용자가 한 명이므로 신체 정보는 하나만 존재해야 합니다. 없으면 생성하고 있으면 수정하며, 엔티티에는 `@Setter` 대신 `updateBody()`, `updateGoal()`만 두어 관련된 값이 함께 바뀌도록 했습니다.

**주간 평균** — 최근 7일 기록을 조회해 기록한 날짜를 `Set`으로 모아, 식사 수가 아닌 **기록한 날 수**로 나눕니다. "오늘"은 서버 위치와 관계없이 `Asia/Seoul` 기준으로 계산합니다.

<br>

## 트러블슈팅

- **브라우저에서 API 키를 보호할 수 없음** — 1차에서 HTTP 리퍼러 제한을 시도했으나 키 발급 방식상 적용 불가 → 키를 서버로 옮겨 브라우저에 노출되지 않는 구조로 전환
- **리팩터링 후 목표 계산 결과가 틀림** — 공통 메서드에 식단 분석 프롬프트가 남아 있었으나 응답 형식이 비슷해 에러 없이 동작 → IDE 경고(`parameter is never used`)로 발견. 에러가 없다고 올바른 것은 아니라는 점을 확인
- **수정한 객체와 저장한 객체가 다름** — 조회 메서드를 여러 번 호출해 서로 다른 객체를 사용 → 한 번 조회한 결과를 재사용하고 DB 조회를 최대 6회에서 1회로 줄임
- **외부 AI 장애가 500으로 전달됨** — `@RestControllerAdvice`로 400·404·503·500을 구분해 응답

자세한 내용과 학습 과정은 [`docs/`](./docs) 폴더에 정리했습니다.

<br>

## 학습 기록

| 문서 | 내용 |
|---|---|
| [01_Spring_기초개념](./docs/01_Spring_기초개념.md) | 계층 구조, 빈, 직렬화, DTO |
| [02_Gemini_프록시_구현](./docs/02_Gemini_프록시_구현.md) | 의존성 주입, RestClient, 예외 처리 |
| [03_DB_연결_JPA](./docs/03_DB_연결_JPA.md) | 엔티티, Repository, 설계 원칙 |
| [04_화면_API_연동](./docs/04_화면_API_연동.md) | 폼과 JSON, PRG, fetch, CORS |

<br>

## 참고

- 화면(HTML·CSS·JavaScript)은 AI 도구의 도움을 받아 작성했고, 이 프로젝트에서는 **API 설계와 서버 구현**에 집중했습니다.
- 화면을 서버 API에 연결할 때 서버 코드는 수정하지 않았습니다.

## 향후 계획

- [ ] 테스트 코드 작성
- [ ] 성별·활동량·목표 종류를 enum으로 리팩터링
- [ ] "오늘" 판단을 서버로 이동 (`GET /api/meals/today`)
- [ ] API 문서 자동화 (Swagger)
- [ ] 배포 및 운영 DB 전환
