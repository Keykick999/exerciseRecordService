package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.WeeklyVolumeDto;
import com.example.fitnessWeb.dto.WeeklyVolumeProjection;
import com.example.fitnessWeb.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>, ExerciseRecordRepositoryCustom {
//    @Query(value = """
//        SELECT
//          /* username은 더이상 필요 없으므로 제거 */
//          YEARWEEK(er.created_at, 1)                                  AS yearWeek,
//          CAST(SUM(es.weight * es.reps) AS DOUBLE)                     AS weeklyVolume
//        FROM users u
//        JOIN exercise_record er ON u.id = er.user_id
//        JOIN exercise_set     es ON er.id = es.exercise_record_id
//        WHERE u.username = :username
//          AND YEARWEEK(er.created_at, 1) = YEARWEEK(:targetDate, 1)
//        GROUP BY YEARWEEK(er.created_at, 1)
//        """, nativeQuery = true)
//    Optional<WeeklyVolumeProjection> findWeeklyVolumeForUserAndDate(
//            @Param("username") String username,
//            @Param("targetDate") LocalDate targetDate
//    );

    @Query("""
        SELECT WeeklyVolumeProjection(                
          YEARWEEK(er.createdAt, 1),
          CAST(SUM(es.weight * es.reps) AS DOUBLE)
                      )
        FROM User u
        JOIN FETCH ExerciseRecord er
        JOIN FETCH ExerciseSet es
        WHERE u.userId = :userId
          AND YEARWEEK(er.createdAt, 1) = YEARWEEK(:targetDate, 1)
        GROUP BY YEARWEEK(er.createdAt, 1)
        """)
    Optional<WeeklyVolumeProjection> findWeeklyVolumeForUserAndDate(
            @Param("userId") String userId,
            @Param("targetDate") LocalDate targetDate
    );
}
