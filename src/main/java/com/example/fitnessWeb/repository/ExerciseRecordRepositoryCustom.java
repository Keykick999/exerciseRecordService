package com.example.fitnessWeb.repository;
import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ExerciseRecordRepositoryCustom {
    Page<ExerciseRecordResponseDto> searchRecords(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String exerciseName,
            List<String> muscleGroupList,
            Pageable pageable
    );

    List<ExerciseSetDetailDto> findExerciseSetDetails(Long recordId, String userId);
}