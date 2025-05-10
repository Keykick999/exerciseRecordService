package com.example.fitnessWeb.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseRecordResponseDto {
    private Long recordId;
    private String exerciseName;
    private LocalDate date;
    private List<ExerciseSetResponseDto> sets;
}