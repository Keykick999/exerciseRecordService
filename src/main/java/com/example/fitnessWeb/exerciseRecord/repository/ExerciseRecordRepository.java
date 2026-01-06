package com.example.fitnessWeb.exerciseRecord.repository;

import com.example.fitnessWeb.exerciseRecord.domain.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


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
//

//    @Query("""
//    SELECT
//        (YEAR(er.createdAt) * 100 + FUNCTION('WEEK', er.createdAt)) AS yearWeek,
//        SUM(es.weight * es.reps) AS weeklyVolume
//    FROM User u
//    JOIN ExerciseRecord er
//    JOIN er.sets es
//    WHERE u.userId = :userId
//      AND FUNCTION('YEARWEEK', er.createdAt) = FUNCTION('YEARWEEK', :targetDate)
//    GROUP BY YEAR(er.createdAt), FUNCTION('WEEK', er.createdAt)
//""")
//    Optional<WeeklyVolumeProjection> findWeeklyVolumeForUserAndDate(
//            @Param("userId") String userId,
//            @Param("targetDate") LocalDate targetDate
//    );
}
