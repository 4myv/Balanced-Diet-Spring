package com.balanceddiet.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter     // Getter 메서드 롬복
@NoArgsConstructor  // 기본 생성자 선언하는 롬복 - 역직렬화(JSON -> Java)에 필요함
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 롬복
public class AnalyzeResponse {  // 서버가 돌려주는 것에 대한 클래스
    private String foodName;    // 음식 이름
    private int calories;       // 칼로리
    private int carb;           // 탄수화물
    private int protein;        // 단백질
    private int fat;            // 지방
}
