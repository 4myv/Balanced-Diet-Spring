package com.balanceddiet.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/onboarding")
    public String onboarding() {
        return "onboarding";
    }
}
