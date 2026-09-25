package com.balanceddiet.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyAverageResponse {
    private int days;
    private int calories;
    private int carb;
    private int protein;
    private int fat;
}
