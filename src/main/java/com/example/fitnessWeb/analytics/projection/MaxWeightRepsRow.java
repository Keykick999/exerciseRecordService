package com.example.fitnessWeb.analytics.projection;

import java.time.LocalDateTime;

public interface MaxWeightRepsRow {
    Long getRecordId();
    LocalDateTime getWorkoutDate();
    Integer getMaxWeight();
    Integer getMaxReps();
}