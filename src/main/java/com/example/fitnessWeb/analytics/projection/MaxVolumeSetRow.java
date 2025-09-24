package com.example.fitnessWeb.analytics.projection;

import java.time.LocalDateTime;

public interface MaxVolumeSetRow {
    Long getRecordId();
    LocalDateTime getCreatedAt();
    Integer getVolume();
    Integer getWeight();
    Integer getReps();
}