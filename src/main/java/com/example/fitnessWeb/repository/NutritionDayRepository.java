package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.NutritionDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface NutritionDayRepository extends JpaRepository<NutritionDay, Long> {
    Optional<NutritionDay> findByUser_UserIdAndDate(String userId, LocalDate date);

}