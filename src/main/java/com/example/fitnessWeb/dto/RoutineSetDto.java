package com.example.fitnessWeb.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RoutineSetDto {
    private Long exerciseTypeId;
    private int setNumber;
    private int weight;
    private int repetitions;
}
