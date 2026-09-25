package com.balanceddiet.server.controller;

import com.balanceddiet.server.dto.GoalCalculateRequest;
import com.balanceddiet.server.dto.GoalResponse;
import com.balanceddiet.server.service.ProfileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goal")
public class GoalController {

    private final ProfileService profileService;

    public GoalController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/calculate")
    public GoalResponse calculate(@RequestBody GoalCalculateRequest request) {
        return profileService.recommendGoal(request);
    }
}
