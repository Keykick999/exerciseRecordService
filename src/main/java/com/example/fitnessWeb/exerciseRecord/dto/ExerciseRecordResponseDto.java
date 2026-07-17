package com.example.fitnessWeb.exerciseRecord.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
@Setter
public class ExerciseRecordResponseDto {
    private Long exerciseRecordId;
    private LocalDateTime modifiedAt;
    private String muscleGroups;

    private transient Set<String> muscleGroupSet = new HashSet<>();

    public ExerciseRecordResponseDto(LocalDateTime modifiedAt, String muscleGroups, Long exerciseRecordId) {
        this.modifiedAt = modifiedAt;
        this.muscleGroups = muscleGroups;
        this.exerciseRecordId = exerciseRecordId;
    }

    // 중간 가공용 생성자
    public ExerciseRecordResponseDto(Long id, LocalDateTime modifiedAt) {
        this.exerciseRecordId = id;
        this.modifiedAt = modifiedAt;
    }
}