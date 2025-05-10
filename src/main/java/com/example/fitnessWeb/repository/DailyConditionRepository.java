package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.DailyCondition;
import com.example.fitnessWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyConditionRepository extends JpaRepository<DailyCondition, Long> {
    Optional<DailyCondition> findByUserAndDate(User user, LocalDate date);
    List<DailyCondition> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}
