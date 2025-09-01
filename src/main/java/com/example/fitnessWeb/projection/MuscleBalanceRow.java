package com.example.fitnessWeb.projection;

import java.math.BigDecimal;

public interface MuscleBalanceRow {
    String getMuscle();
    Long getVolume();
    Long getTotalVolume();
    BigDecimal getPct();
}