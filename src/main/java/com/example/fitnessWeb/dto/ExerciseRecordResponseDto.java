package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ExerciseRecordResponseDto {
    private LocalDateTime createdAt;
    private String muscleGroups;
    private Long exerciseRecordId;

    private transient Set<String> muscleGroupSet = new HashSet<>();

    public ExerciseRecordResponseDto(LocalDateTime createdAt, String muscleGroups, Long exerciseRecordId) {
        this.createdAt = createdAt;
        this.muscleGroups = muscleGroups;
        this.exerciseRecordId = exerciseRecordId;
    }

    // 중간 가공용 생성자
    public ExerciseRecordResponseDto(Long id, LocalDateTime createdAt) {
        this.exerciseRecordId = id;
        this.createdAt = createdAt;
    }
}