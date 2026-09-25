package com.balanceddiet.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoalSaveRequest {
    private String goalType;        // 목표 타입
    private String customGoalText;  // '기타' 목표 텍스트
    private int calories;           // 목표 칼로리
    private int carb;               // 목표 탄수화물
    private int protein;            // 목표 단백질
    private int fat;                // 목표 지방
}
