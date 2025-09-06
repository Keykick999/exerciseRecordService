package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.projection.MaxRepsRow;
import com.example.fitnessWeb.projection.MaxVolumeSetRow;
import com.example.fitnessWeb.projection.MaxWeightRepsRow;
import com.example.fitnessWeb.projection.MuscleBalanceRow;
import org.springframework.data.domain.Pageable;
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


    @Query(value = """
        SELECT 
            er.id AS recordId,
            er.created_at AS workoutDate,
            MAX(es.reps) AS maxReps
        FROM exercise_record er
        JOIN exercise_set es
          ON es.exercise_record_id = er.id
        WHERE er.user_id = :userId
          AND es.exercise_type_id = :exerciseTypeId
          AND es.weight = :weight
        GROUP BY er.id, er.created_at
        ORDER BY er.created_at DESC, er.id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
          SELECT er.id
          FROM exercise_record er
          JOIN exercise_set es ON es.exercise_record_id = er.id
          WHERE er.user_id = :userId
            AND es.exercise_type_id = :exerciseTypeId
            AND es.weight = :weight
          GROUP BY er.id
        ) t
        """,
            nativeQuery = true)
    List<MaxRepsRow> findRecentMaxRepsByWeight(
            @Param("userId") Long userId,
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("weight") Integer weight,
            Pageable pageable
    );

    @Query(value = """
        SELECT 
            t.record_id       AS recordId,
            t.workout_date    AS workoutDate,
            t.weight          AS maxWeight,
            t.reps            AS maxReps
        FROM (
            SELECT
                er.id            AS record_id,
                er.created_at    AS workout_date,
                es.weight,
                es.reps,
                DENSE_RANK() OVER (
                    PARTITION BY er.id
                    ORDER BY es.weight DESC
                ) AS w_rank,
                ROW_NUMBER() OVER (
                    PARTITION BY er.id, es.weight
                    ORDER BY es.reps DESC, es.set_order ASC, es.id ASC
                ) AS r_rank
            FROM exercise_record er
            JOIN exercise_set es ON es.exercise_record_id = er.id
            WHERE er.user_id = :userId
              AND es.exercise_type_id = :exerciseTypeId
        ) t
        WHERE t.w_rank = 1
          AND t.r_rank = 1
        ORDER BY t.workout_date DESC, t.record_id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
            SELECT er.id
            FROM exercise_record er
            JOIN exercise_set es ON es.exercise_record_id = er.id
            WHERE er.user_id = :userId
              AND es.exercise_type_id = :exerciseTypeId
            GROUP BY er.id
        ) x
        """,
            nativeQuery = true)
    List<MaxWeightRepsRow> findMaxWeightRepsHistory(
            @Param("userId") Long userId,
            @Param("exerciseTypeId") Long exerciseTypeId,
            Pageable pageable
    );


    @Query(value = """
        SELECT
            t.record_id    AS recordId,
            t.created_at   AS createdAt,
            t.volume       AS volume,
            t.weight       AS weight,
            t.reps         AS reps
        FROM (
            SELECT
                er.id                 AS record_id,
                er.created_at         AS created_at,
                es.weight             AS weight,
                es.reps               AS reps,
                (es.weight * es.reps) AS volume,
                ROW_NUMBER() OVER (
                    PARTITION BY er.id
                    ORDER BY (es.weight * es.reps) DESC,
                             es.weight DESC,
                             es.reps   DESC,
                             es.set_order ASC,
                             es.id     ASC
                ) AS r_rank
            FROM exercise_record er
            JOIN exercise_set es
              ON es.exercise_record_id = er.id
            WHERE er.user_id = :userId
              AND (:startDate IS NULL OR er.created_at >= :startDate)
              AND (:endDate   IS NULL OR er.created_at <  :endDate)
              AND (:exerciseTypeId IS NULL OR es.exercise_type_id = :exerciseTypeId)
        ) t
        WHERE t.r_rank = 1
        ORDER BY t.created_at DESC, t.record_id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM (
            SELECT er.id
            FROM exercise_record er
            JOIN exercise_set es ON es.exercise_record_id = er.id
            WHERE er.user_id = :userId
              AND (:startDate IS NULL OR er.created_at >= :startDate)
              AND (:endDate   IS NULL OR er.created_at <  :endDate)
              AND (:exerciseTypeId IS NULL OR es.exercise_type_id = :exerciseTypeId)
            GROUP BY er.id
        ) x
        """,
            nativeQuery = true)
    List<MaxVolumeSetRow> findMaxVolumeSets(
            @Param("userId") Long userId,
            @Param("exerciseTypeId") Long exerciseTypeId,   // nullable
            @Param("startDate") LocalDateTime startDate,     // nullable
            @Param("endDate") LocalDateTime endDate,         // nullable
            Pageable pageable
    );
}