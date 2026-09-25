package com.balanceddiet.server.service;

import com.balanceddiet.server.domain.Meal;
import com.balanceddiet.server.domain.MealRepository;
import com.balanceddiet.server.dto.MealRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    // 식단 저장
    public Meal save(MealRequest request) {
        // 1. 값 검사
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("음식 이름을 입력해주세요");
        }
        if (request.getCalories() < 0 || request.getCarb() < 0 || request.getProtein() < 0 || request.getFat() < 0) {
            throw new IllegalArgumentException("영양소 값은 0 이상이어야 해요");
        }

        // 2. 오늘 날짜 구하기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // LocalDate.now(ZoneId.of("Asia/Seoul")); -> 서울 기준 오늘 날짜를 구한다

        // 3. Meal 객체 만들기
        Meal meal = new Meal(today, request.getName(), request.getCalories(), request.getCarb(), request.getProtein(), request.getFat());

        // 4. 저장하고 돌려주기
        return mealRepository.save(meal);
        // 저장 후 id가 채워진 Meal을 돌려줌
    }

    // 날짜별 조회
    public List<Meal> findByDate(LocalDate date) {
        return mealRepository.findByDate(date);
        // date와 같은 날짜에 기록된 식단 목록을 찾음
    }

    // 삭제
    public void delete(Long id) {
        if (mealRepository.existsById(id)) {
            // Id값이 존재하는지 검사
            mealRepository.deleteById(id);
            // Id에 해당하는 값을 지움
        } else {
            // Id가 존재하지 않은 경우
            throw new IllegalArgumentException("존재하지 않는 기록이에요");
        }
    }
}
