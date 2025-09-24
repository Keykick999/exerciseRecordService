package com.example.fitnessWeb.exerciseVideo.dto;

import com.example.fitnessWeb.exerciseVideo.enums.ExerciseVideoType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseVideoResponse {
    private Long id;
    private ExerciseVideoType type;
    private String iframeSrc;
    private List<ExerciseVideoDetailResponse> details;
}