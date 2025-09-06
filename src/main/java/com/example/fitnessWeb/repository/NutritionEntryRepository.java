package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.NutritionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface NutritionEntryRepository extends JpaRepository<NutritionEntry, Long> {

    @Query("""
        select coalesce(sum(e.caloriesKcal), 0),
               coalesce(sum(e.proteinG), 0),
               coalesce(sum(e.carbsG), 0),
               coalesce(sum(e.fatG), 0)
        from NutritionEntry e
        where e.user.userId = :userId
          and e.consumedAt >= :startInclusive
          and e.consumedAt <  :endExclusive
          and e.deleted = false
    """)
    Object[] sumActiveByUserAndConsumedAtBetween(String userId,
                                                 LocalDateTime startInclusive,
                                                 LocalDateTime endExclusive);
}