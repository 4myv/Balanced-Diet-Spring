package com.balanceddiet.server.controller;

import com.balanceddiet.server.domain.Meal;
import com.balanceddiet.server.dto.MealRequest;
import com.balanceddiet.server.dto.MealResponse;
import com.balanceddiet.server.service.MealService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    public MealResponse save(@RequestBody MealRequest request) {
        return new MealResponse(mealService.save(request));
    }

    // @RequestParam -> 주소 뒤 ?date=... 의 값을 받음
    // @DateTimeFormat(...) -> 이 모양의 글자를 날짜로 바꿔라
    @GetMapping
    public List<MealResponse> findByDate(
            @RequestParam @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MealResponse> result = new ArrayList<>();
        for (Meal meal : mealService.findByDate(date)) {
            result.add(new MealResponse(meal));
        }
        return result;
    }

    @DeleteMapping("/{id}")
    // {id}에 PathVariable을 사용해서 id 변수를 넣는다.
    public void delete(@PathVariable Long id) {
        mealService.delete(id);
    }
}
