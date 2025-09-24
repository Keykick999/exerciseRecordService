package com.example.fitnessWeb.exerciseVideo.dto;

import com.example.fitnessWeb.exerciseVideo.enums.ExerciseVideoType;
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
