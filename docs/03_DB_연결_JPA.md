**DB 기본**

테이블 - 릴레이션

행 - row, 레코드, 튜플

열 - column, 필드

H2 - 설치 없이 쓰는 가벼운 DB, 파일 모드로 쓰면 서버를 꺼도 데이터가 남음



**설정 (application.properties)**

|**설정**|**뜻**|
|-|-|
|spring.datasource.url=jdbc:h2:file:./data/...|DB 파일 위치|
|spring.jpa.hibernate.ddl-auto=update|엔티티를 보고 테이블을 자동으로 만들거나 고침|
|spring.jpa.show-sql=true|JPA가 실행하는 SQL을 콘솔에 보여줌|
|spring.h2.console.enabled=true|브라우저에서 DB를 보는 화면 (/h2-console)|
|spring.datasource.username/password|DB 접속 정보 - DB 파일이 처음 만들어질 때 정해짐|

* DB 파일은 내 데이터라 .gitignore에 data/ 추가
* H2 콘솔은 배포할 땐 꺼야 함 (브라우저로 DB를 볼 수 있어서)



**JPA**

* Jackson이 JSON을 바꿔주듯 객체 <-> DB 행을 바꿔주는 도구



**Entity**

* DB 테이블과 연결된 클래스 (객체 하나 = 행 하나)

```java
@Entity
@Getter
@NoArgsConstructor
public class Meal {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
}
```

* 기본형 (int, double)은 자동으로 not null 이기 때문에 String, LocalDate 처럼 null이 될 수 있는 것에만 nullable=false 명시
* @Setter를 안 붙이는 이유 - DB와 직결된 값을 아무 곳에서나 하나씩 바꾸지 못하게
* 값을 바꿔야 하면 바꾸는 방법을 엔티티가 메서드로 정함 (updateBody, updateGoal) -> 관련된 값들이 한 번에 바뀜



**어노테이션**

* **@Entity :** 이 클래스는 테이블과 연결됨. 클래스 이름 -> 테이블 이름
* **@Id :** DB의 Primary Key (Unique, Not Null), 없으면 서버가 안 켜짐
* **@GeneratedValue(IDENTITY) :** 번호는 DB가 1, 2, 3.. 과 같이 자동을 채워줌
* **@Column(nullable = false) :** Not Null 제약 조건을 붙임
* **@DateTimeFormat(iso = DateTimeformat.ISO.DATE) :** 주소로 온 글자를 LocalDate로 바꿔라



**Repository**

```java
public interface MealRepository extends JpaRepository<Meal, Long> {
	   List<Meal> findByDate(LocalDate date);
	   List<Meal> findByDateBetween(LocalDate start, LocalDate end);
}
```

* 인터페이스만 만들면 구현은 스프링이 자동으로 만들어서 빈으로 등록 -> @Repository  불필요
* JpaRepository<다룰 엔티티, @Id 자료형>
* 기본으로 존재하는 메서드 : save, findById, findAll, deleteById, existsById, count
* 메서드 이름을 읽고 SQL을 만들어줌 - find + By + Date(필드 이름) + Between(양 끝 포함)
-> SELECT date FROM meal WHERE BETWEEN start AND end;



**save()가 하는 두 가지 일**

* JPA가 id를 보고 알아서 판단
id가 null -> INSERT
id가 있음 -> UPDATE
* 저장 후 id가 채워진 객체를 돌려줌



**Set이란?**

* 같은 값을 한 번만 담는 모음 (중복 X)
* **List와의 차이**

||**List(ArrayList)**|**Set(HashSet)**|
|-|-|-|
|**같은 값**|여러 번 담음|한 번만 담음|
|**순서**|넣은 순서 유지|순서 없음 (Hash)|
|**번호로 꺼내기** |get(0) 가능|불가능 (번호가 없음)|
|**쓰는 곳**|순서대로 나열할 때|중복을 없애거나 "있나 없나"만 볼 때|

* **자주 쓰는 메서드**

|**메서드**|**하는 일**|
|-|-|
|add(값)|넣기. 새로 들어가면 true, 있으면 false를 돌려줌|
|contains(값)|있는지 확인|
|size()|몇 개인지|
|remove(값)|빼기|
|isEmpty|비어있는지 |





**Controller와 응답**

|**어노테이션**|**주소 예시**|**받는 것**|
|-|-|-|
|**@RequestBody**|본문 JSON|DTO 객체|
|**@RequestParam**|/api/meals?date=2026-09-25|? 뒤의 값|
|**@PathVariable**|/api/meals/{id}|주소의 {id} 자리|

* Mapping에 괄호가 비면 클래스 주소 그대로



**ResponseEntity**

``` java
ResponseEntity.ok(내용)              // 200 + 내용, 바로 완성
ResponseEntity.notFound().build()    // 404, 본문 없이 완성
ResponseEntity.status(400).body(내용) // 원하는 코드 + 내용으로 완성
```

* static 메서드라서 ResponseEntity.으로 바로 부를 수 있음
* notFound()는 상태 코드(404)를 의미 
* build() -> 본문 없이 보냄
* body() -> 괄호 안에 본문 입력



**설계 원칙**

* 엔티티는 밖에 그대로 내보내지 않는다 -> 받을 땐 Request DTO,  돌려줄 땐 Response DTO
* DTO는 용도마다 따로 만들기 -> GoalRequest(AI 계산용) / GoalSaveRequest(저장용) / 
GoalCalculateRequest(목표 종류만 요청받아 계산 넘김) / GoalResponse(계산 결과) /
 ProfileResponse(저장된 데이터)
* 엔티티 -> DTO 변환은 DTO의 생성자로 함 - ex) new MealResponse(meal)
* 계산에서 새로 만든 값은 처음부터 DTO로 만들어 돌려줌 - ex) WeeklyAverageResponse\\
* 서버가 정할 값은 요청으로 받지 않는다 -> 날짜는 서버가 직접 '오늘' 날짜로
* Service가 다른 Service를 쓸 수 있다 - ex) ProfileService -> GeminiService
* 값 검사 기준은 의미에 따라 달라짐 -> 신체 정보는 <= 0, 음식 영양소는 < 0 (한 음식에서 영양소는 0이 될 수 있음) 
* 시간대는 명시 - LocalDate.now(ZoneId.of("Asia/Seoul")) - HTTP 헤더의 Date는 규칙상 항상 GMT



**Git**

* 커밋 순서 - 쓰이는 쪽부터 (Entity -> Repository -> DTO -> Service -> Controller)
그래야 각 커밋 시점마다 코드가 실행되는 상태
* 같은 목적의 변경이 여러 파일에 걸치면 한 번에 커밋 



