package com.example.fitnessWeb.analytics.repository;

import com.example.fitnessWeb.analytics.projection.MaxWeightRepsRow;
import java.util.List;

public interface AnalyticsNativeRepository {
    List<MaxWeightRepsRow> findMaxWeightRepsHistory(
            String userId,
            Long exerciseTypeId,
            int limit,
            int offset
    );
}