package com.balanceddiet.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private int calories;   // 칼로리
    private int carb;       // 탄수화물
    private int protein;    // 단백질
    private int fat;        // 지방
}
