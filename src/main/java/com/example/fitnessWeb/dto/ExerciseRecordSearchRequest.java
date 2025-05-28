package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ExerciseRecordSearchRequest {
    private LocalDateTime startDate;       // 조회 시작일
    private LocalDateTime endDate;         // 조회 종료일
    private String exerciseName;       // 운동 이름 (ex: 벤치프레스)
    private String muscleGroup;        // 운동 부위 (ex: 가슴)
}
