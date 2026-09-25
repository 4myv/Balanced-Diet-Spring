package com.balanceddiet.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity     // 이 클래스는 DB 테이블과 연결됨
@Getter
@NoArgsConstructor
public class Meal {

    @Id     // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 자료형이 아니라서 null이 될 수 있는 LocalDate, String에만 not null
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private String name;

    private int calories;
    private int carb;
    private int protein;
    private int fat;

    public Meal(LocalDate date, String name, int calories, int carb, int protein, int fat) {
        this.date = date;
        this.name = name;
        this.calories = calories;
        this.carb = carb;
        this.protein = protein;
        this.fat = fat;
    }
}
