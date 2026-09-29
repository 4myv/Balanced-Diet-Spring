package com.balanceddiet.server.controller;

import com.balanceddiet.server.domain.Profile;
import com.balanceddiet.server.dto.ProfileRequest;
import com.balanceddiet.server.dto.ProfileResponse;
import com.balanceddiet.server.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PageController {

    private final ProfileService profileService;

    public PageController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/onboarding")
    public String onboarding(Model model) {
        Profile profile = profileService.find();
        if (profile != null) {
            model.addAttribute("profile", new ProfileResponse(profile));
        }
        return "onboarding";
    }

    @PostMapping("/onboarding")
    public String saveBody(@ModelAttribute ProfileRequest request, Model model) {
        try {
            profileService.saveBody(request);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("profile", request);
            return "onboarding";
        }
        return "redirect:/goal";
    }
}
