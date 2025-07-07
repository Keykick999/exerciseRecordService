package com.example.fitnessWeb.dto;

import com.example.fitnessWeb.entity.ExerciseVideoType;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExerciseVideoSearchRequest {
    private ExerciseVideoType type;
    private List<String> muscleGroups;
    private List<String> exerciseNames;
    private List<String> athleteNames;
}
