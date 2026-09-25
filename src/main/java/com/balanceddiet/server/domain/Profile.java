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
    private Long id;

    private double height;
    private double weight;
    private int age;
    @Column(nullable = false)
    private String gender;
    @Column(nullable = false)
    private String activity;

    private String goalType;
    private String customGoalText;
    private int goalCalories;
    private int goalCarb;
    private int goalProtein;
    private int goalFat;

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
