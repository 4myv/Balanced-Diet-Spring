package com.balanceddiet.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoalCalculateRequest {
    private String goalType;
    private String customGoalText;
}
