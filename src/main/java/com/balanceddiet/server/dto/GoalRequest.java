package com.balanceddiet.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    private double height;      // 키
    private double weight;      // 몸무게
    private int age;            // 나이
    private String gender;      // 성별
    private String activity;    // 활동량
    private String goalType;    // 목표 -> 벌크업/다이어트/건강관리
    private String customGoalText;  // 목표 (사용자가 '기타'를 선택했을 때)
}
