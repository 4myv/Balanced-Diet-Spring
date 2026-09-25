package com.balanceddiet.server.controller;

import com.balanceddiet.server.domain.Meal;
import com.balanceddiet.server.dto.MealRequest;
import com.balanceddiet.server.dto.MealResponse;
import com.balanceddiet.server.dto.WeeklyAverageResponse;
import com.balanceddiet.server.service.MealService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 식단 기록 API - 저장, 일별 조회, 삭제, 주간 평균
@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    // 스프링이 만들어둔 MealService를 넣어줌 (의존성 주입)
    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    // Post /api/meals - 괄호가 비어 있으면 클래스 주소 그대로
    @PostMapping
    public MealResponse save(@RequestBody MealRequest request) {
        // 요청 본문 JSON -> MealRequest로 받음

        return new MealResponse(mealService.save(request));
        // Service가 저장한 Meal(엔티티)을 MealResponse로 바꿔서 돌려줌
        // -> 엔티티를 밖에 그대로 내보내지 않기 위해
    }

    // GET /api/meals?date=예시)2026-09-25
    // @RequestParam -> 주소 뒤 ?date=... 의 값을 받음
    // @DateTimeFormat(...) -> "예시) 2026-09-25" 모양의 글자를 LocalDate로 바꿔라
    @GetMapping
    public List<MealResponse> findByDate(
            @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MealResponse> result = new ArrayList<>();
        for (Meal meal : mealService.findByDate(date)) {
            result.add(new MealResponse(meal));
            // 목록에서 Meal을 하나씩 꺼내 MealResponse 생성자로 바꿔서 담음
        }
        return result;
    }

    // DELETE /api/meals/{id}
    @DeleteMapping("/{id}")
    // {id}에 @PathVariable을 사용해서 id 변수를 넣는다.
    public void delete(@PathVariable Long id) {
        mealService.delete(id);
        // 있는 id면 삭제,
        // 없는 아이디면 Service가 IllegalArgumentException을 던짐 -> 400
    }

    // GET /api/meals/weekly-average
    // 최근 7일 중 기록한 날 기준 하루 평균
    @GetMapping("/weekly-average")
    public WeeklyAverageResponse getWeeklyAverage() {
        return mealService.getWeeklyAverage();
    }
}
