package com.balanceddiet.server.service;

import com.balanceddiet.server.domain.Meal;
import com.balanceddiet.server.domain.MealRepository;
import com.balanceddiet.server.dto.MealRequest;
import com.balanceddiet.server.dto.WeeklyAverageResponse;
import com.balanceddiet.server.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            // Id가 존재하지 않은 경우 -> NotFoundException을 만들어 던짐 -> 404
            throw new NotFoundException("존재하지 않는 기록이에요");
        }
    }

    // 최근 7일 중 기록한 날 기준 하루 평균
    public WeeklyAverageResponse getWeeklyAverage() {
        // 1. 기간 정하기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate start = today.minusDays(6);   // 오늘을 포함한 7일이라 -6일

        // 2. 기록 꺼내기
        List<Meal> meals = mealRepository.findByDateBetween(start, today);

        // 3. 칼로리·탄·단·지 합계와 기록한 날 세기
        int totalCalories = 0;
        int totalCarb = 0;
        int totalProtein = 0;
        int totalFat = 0;
        Set<LocalDate> recordedDays = new HashSet<>();
        for (Meal meal : meals) {
            recordedDays.add(meal.getDate());
            totalCalories += meal.getCalories();
            totalCarb += meal.getCarb();
            totalProtein += meal.getProtein();
            totalFat += meal.getFat();
        }
        int days = recordedDays.size();

        // 4. 기록이 하나도 없으면
        if (days == 0) {
            return new WeeklyAverageResponse(0, 0, 0, 0, 0);
        }

        // 5. 평균 구하기
        // int끼리 나누면 소수점을 버리기 때문에, double로 바꿔 나눈 뒤 반올림하고 int로 형변환
        int avgCalories = (int) Math.round((double) totalCalories / days);
        int avgCarb = (int) Math.round((double) totalCarb / days);
        int avgProtein = (int) Math.round((double) totalProtein / days);
        int avgFat = (int) Math.round((double) totalFat / days);

        return new WeeklyAverageResponse(days, avgCalories, avgCarb, avgProtein, avgFat);
    }
}
