package com.balanceddiet.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileRequest {
    private double height;      // 키
    private double weight;      // 몸무게
    private int age;            // 나이
    private String gender;      // 성별
    private String activity;    // 활동량
}
