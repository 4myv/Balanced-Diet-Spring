package com.balanceddiet.server.controller;

import com.balanceddiet.server.domain.Profile;
import com.balanceddiet.server.dto.GoalSaveRequest;
import com.balanceddiet.server.dto.ProfileRequest;
import com.balanceddiet.server.dto.ProfileResponse;
import com.balanceddiet.server.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // 신체 정보 저장 메서드
    // POST /api/profile - 괄호가 비어 있으면 클래스 주소 그대로
    @PostMapping
    public ProfileResponse saveBody(@RequestBody ProfileRequest request) {
        // ProfileRequest 형태의 request를 profileService의 saveBody 메서드를 거쳐
        // Profile 형태로 반환받고 new ProfileResponse() 생성자에 넘긴다
        return new ProfileResponse(profileService.saveBody(request));
    }

    // 목표 저장 메서드
    // POST /api/profile/goal
    @PostMapping("/goal")
    public ProfileResponse saveGoal(@RequestBody GoalSaveRequest request) {
        // GoalSaveRequest 형태의 request를 profileService의 saveGoal 메서드를 거쳐
        // Profile 형태로 반환받고 new ProfileResponse() 생성자에 넘긴다
        return new ProfileResponse(profileService.saveGoal(request));
    }

    // 조회 메서드
    // GET /api/profile - 괄호가 비어 있으면 클래스 주소 그대로
    @GetMapping
    public ResponseEntity<ProfileResponse> find() {
        // find()는 Profile이 없으면 null을 돌려줌
        // -> null을 받아 생성자에 넣을 경우, null.getID()가 돼서 에러 발생
        // 따라서 null값을 반환하지 않고 404 상태 코드를 돌려준다
        Profile profile = profileService.find();

        // find()로 값을 받은 profile이 null인지 검사
        if (profile == null) {
            return ResponseEntity.notFound().build();
            // notFound() -> 404 상태, build() -> 본문 없이 보냄
        }
        return ResponseEntity.ok(new ProfileResponse(profile));
        // ok() -> 상태 코드 200과 괄호 안에 본문을 넣어 보냄
    }
}
