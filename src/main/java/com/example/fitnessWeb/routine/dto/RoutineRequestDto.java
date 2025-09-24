package com.example.fitnessWeb.routine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RoutineRequestDto {
    private String name;
    private List<RoutineSetDto> routineSets;
}
