package com.balanceddiet.server.service;

import com.balanceddiet.server.domain.Profile;
import com.balanceddiet.server.domain.ProfileRepository;
import com.balanceddiet.server.dto.GoalSaveRequest;
import com.balanceddiet.server.dto.ProfileRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    // 생성자
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // 저장된 Profile 꺼내기 - 없으면 null
    public Profile find() {
        List<Profile> profiles = profileRepository.findAll();
        if (profiles.isEmpty()) {
            return null;
        } else {
            return profiles.get(0);
        }
    }

    // 신체 정보 저장 - 없으면 새로 만들고, 있으면 업데이트
    public Profile saveBody(ProfileRequest request) {

        // 1. 값 검사
        if (request.getHeight() <= 0 || request.getWeight() <= 0 || request.getAge() <= 0) {
            throw new IllegalArgumentException("신체 정보를 올바르게 입력해주세요");
        }
        if (request.getGender() == null || request.getGender().isBlank() || request.getActivity() == null || request.getActivity().isBlank()) {
            throw new IllegalArgumentException("성별과 활동량을 선택해주세요");
        }

        // 2. 기존 Profile 찾기
        Profile profile = find();

        // 3. null을 받았으면 새로 만들고, 아니면 업데이트 하기
        if (profile == null) {
            profile = new Profile(request.getHeight(), request.getWeight(), request.getAge(), request.getGender(), request.getActivity());
        } else {
            profile.updateBody(request.getHeight(), request.getWeight(), request.getAge(), request.getGender(), request.getActivity());
        }

        // 4. 저장하고 돌려주기
        return profileRepository.save(profile);
    }

    // 목표 저장 - 신체 정보가 먼저 있어야 함
    public Profile saveGoal(GoalSaveRequest request) {

        // 1. Profile이 있는지 찾기
        Profile profile = find();
        if (profile == null) {
            throw new IllegalArgumentException("신체 정보를 먼저 입력해주세요");
        }

        // 2. 값 검사
        if (request.getGoalType() == null || request.getGoalType().isBlank()) {
            throw new IllegalArgumentException("목표를 선택해주세요");
        }
        if ("custom".equals(request.getGoalType()) && (request.getCustomGoalText() == null || request.getCustomGoalText().isBlank())) {
            throw new IllegalArgumentException("목표를 직접 입력해주세요");
        }
        if (request.getCalories() <= 0) {
            throw new IllegalArgumentException("목표 칼로리를 올바르게 입력해주세요");
        }
        if (request.getCarb() < 0 || request.getProtein() < 0 || request.getFat() < 0) {
            throw new IllegalArgumentException("영양소 값은 0 이상이어야 해요");
        }

        // 3. 목표 채우기
        profile.updateGoal(request.getGoalType(), request.getCustomGoalText(), request.getCalories(), request.getCarb(), request.getProtein(), request.getFat());

        // 4. 저장하고 돌려주기
        return profileRepository.save(profile);
    }
}
