package com.example.fitnessWeb.dto;

import com.example.fitnessWeb.entity.ExerciseVideoType;
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