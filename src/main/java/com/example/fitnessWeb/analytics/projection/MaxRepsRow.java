package com.example.fitnessWeb.analytics.projection;

import java.time.LocalDateTime;

// 네이티브 쿼리 결과를 매핑할 Projection
public interface MaxRepsRow {
    Long getRecordId();            // er.id AS recordId
    LocalDateTime getWorkoutDate();// er.created_at AS workoutDate
    Integer getMaxReps();          // MAX(es.reps) AS maxReps
}