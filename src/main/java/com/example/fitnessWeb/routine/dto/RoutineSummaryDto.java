package com.example.fitnessWeb.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RoutineSummaryDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
