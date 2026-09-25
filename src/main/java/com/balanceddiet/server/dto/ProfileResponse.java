package com.balanceddiet.server.dto;

import com.balanceddiet.server.domain.Profile;
import lombok.Getter;

@Getter
public class ProfileResponse {
    private final Long id;

    private final double height;
    private final double weight;
    private final int age;
    private final String gender;
    private final String activity;

    private final String goalType;
    private final String customGoalText;
    private final int goalCalories;
    private final int goalCarb;
    private final int goalProtein;
    private final int goalFat;

    public ProfileResponse(Profile profile) {
        this.id = profile.getId();
        this.height = profile.getHeight();
        this.weight = profile.getWeight();
        this.age = profile.getAge();
        this.gender = profile.getGender();
        this.activity = profile.getActivity();
        this.goalType = profile.getGoalType();
        this.customGoalText = profile.getCustomGoalText();
        this.goalCalories = profile.getGoalCalories();
        this.goalCarb = profile.getGoalCarb();
        this.goalProtein = profile.getGoalProtein();
        this.goalFat = profile.getGoalFat();
    }
}
