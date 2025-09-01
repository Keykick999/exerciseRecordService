package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.projection.MuscleBalanceRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<com.example.fitnessWeb.entity.ExerciseRecord, Long> {
    @Query(value = """
        SELECT
          m.name AS muscle,
          COALESCE(b.muscle_vol, 0) AS volume,
          COALESCE(SUM(b.muscle_vol) OVER (), 0) AS total_volume,
          CASE 
            WHEN COALESCE(SUM(b.muscle_vol) OVER (), 0) > 0
            THEN ROUND(100 * COALESCE(b.muscle_vol, 0) 
                            / COALESCE(SUM(b.muscle_vol) OVER (), 0), 1)
            ELSE 0
          END AS pct
        FROM muscle m
        LEFT JOIN (
          SELECT
            em.muscle_id,
            SUM(es.weight * es.reps) AS muscle_vol
          FROM exercise_record er
          JOIN exercise_set es  ON es.exercise_record_id = er.id
          JOIN exercise_type et ON et.id = es.exercise_type_id
          JOIN exercise_muscle em ON em.exercise_type_id = et.id
          JOIN users u ON u.id = er.user_id
          WHERE u.username = :username
            AND er.created_at >= :startDate
            AND er.created_at <  :endDate
          GROUP BY em.muscle_id
        ) b ON b.muscle_id = m.id
        ORDER BY pct DESC, muscle
        """, nativeQuery = true)
    List<MuscleBalanceRow> findMuscleBalance(
            @Param("username") String username,
            @Param("startDate") LocalDateTime start,
            @Param("endDate") LocalDateTime end
    );
}