package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.WeeklyVolumeDto;
import com.example.fitnessWeb.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>, ExerciseRecordRepositoryCustom {
    @Query(
      value = """
        SELECT
          u.username                            AS username,
          YEARWEEK(er.created_at, 1)           AS yearWeek,
          SUM(es.weight * es.reps)             AS weeklyVolume
        FROM users u
        JOIN exercise_record er ON u.id = er.user_id
        JOIN exercise_set     es ON er.id = es.exercise_record_id
        GROUP BY u.username, YEARWEEK(er.created_at, 1)
        ORDER BY u.username, YEARWEEK(er.created_at, 1)
        """,
      nativeQuery = true
    )
    List<WeeklyVolumeDto> findAllWeeklyVolumes();
}
