package com.balanceddiet.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 기본 키

    private double height;      // 키
    private double weight;      // 몸무게
    private int age;            // 나이
    @Column(nullable = false)
    private String gender;      // 성별
    @Column(nullable = false)
    private String activity;    // 활동량

    private String goalType;        // 목표 타입
    private String customGoalText;  // '기타' 목표 텍스트
    private int goalCalories;       // 목표 칼로리
    private int goalCarb;           // 목표 탄수화물
    private int goalProtein;        // 목표 단백질
    private int goalFat;            // 목표 지방

    // 신체 정보 생성자
    public Profile(double height, double weight, int age, String gender, String activity) {
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.activity = activity;
    }

    // 신체 정보 수정
    public void updateBody(double height, double weight, int age, String gender, String activity) {
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.gender = gender;
        this.activity = activity;
    }

    // 목표 저장
    public void updateGoal(String goalType, String customGoalText, int goalCalories, int goalCarb, int goalProtein, int goalFat) {
        this.goalType = goalType;
        this.customGoalText = customGoalText;
        this.goalCalories = goalCalories;
        this.goalCarb = goalCarb;
        this.goalProtein = goalProtein;
        this.goalFat = goalFat;
    }
}
