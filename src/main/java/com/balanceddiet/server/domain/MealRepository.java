package com.balanceddiet.server.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    //                                     <다루는 엔티티, @Id 필드의 자료형>
    List<Meal> findByDate(LocalDate date);
    // 스프링이 메서드 이름을 읽고 DB 명령을 만들어줌
    // findByDate -> find(찾아라) By(~로) Date(date 필드)
    // -> "date가 같은 행을 전부 찾아라"

    List<Meal> findByDateBetween(LocalDate start, LocalDate end);
    // 스프링이 메서드 이름을 읽고 DB 명령을 만들어줌
    // findByDateBetween -> find(찾아라) By(~로)
    // Date(date 필드) Between(start부터 end까지, 양 끝 포함)
}
