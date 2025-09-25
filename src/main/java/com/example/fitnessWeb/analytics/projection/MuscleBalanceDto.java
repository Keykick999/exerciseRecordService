package com.example.fitnessWeb.analytics.projection;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class MuscleBalanceDto {
    private String muscle;
    private Double volume;
    private Double totalVolume;
    private Double pct;
}
