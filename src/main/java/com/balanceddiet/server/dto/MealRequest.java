package com.balanceddiet.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MealRequest {
    private String name;    // 음식 이름
    private int calories;   // 칼로리
    private int carb;       // 탄수화물
    private int protein;    // 단백질
    private int fat;        // 지방
}
