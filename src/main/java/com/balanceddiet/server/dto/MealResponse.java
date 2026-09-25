package com.balanceddiet.server.dto;

import com.balanceddiet.server.domain.Meal;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MealResponse {
    private final Long id;
    private final LocalDate date;
    private final String name;
    private final int calories;
    private final int carb;
    private final int protein;
    private final int fat;

    public MealResponse(Meal meal) {
        this.id = meal.getId();
        this.date = meal.getDate();
        this.name = meal.getName();
        this.calories = meal.getCalories();
        this.carb = meal.getCarb();
        this.protein = meal.getProtein();
        this.fat = meal.getFat();
    }
}
