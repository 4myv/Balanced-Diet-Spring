package com.balanceddiet.server.controller;

import com.balanceddiet.server.dto.GoalRequest;
import com.balanceddiet.server.dto.GoalResponse;
import com.balanceddiet.server.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goal")
public class GoalController {

    private final GeminiService geminiService;

    public GoalController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/calculate")
    public GoalResponse calculate(@RequestBody GoalRequest request) {
        return geminiService.calculateGoal(request);
    }
}
