package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.dto.ExerciseSetDto;
import com.example.fitnessWeb.service.ExerciseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {

    private final ExerciseRecordService service;


    // 운동 기록 저장
    @PostMapping
    public ResponseEntity<String> save(Authentication auth,
                                       @RequestBody @Valid List<ExerciseSetDto> dto,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        service.saveRecord(userDetails.getUsername(), dto);
        return ResponseEntity.ok("운동 기록 저장 완료");
    }


    // 운동 기록 조회
    @GetMapping
    public ResponseEntity<Page<ExerciseRecordResponseDto>> getExerciseRecord(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String exerciseName,
            @RequestParam(required = false) List<String> muscleGroup,
            Authentication auth,
            Pageable pageable) {
        // 날짜 파싱 (문자열 → LocalDateTime)
        LocalDateTime start = (startDate != null && !startDate.isBlank())
                ? LocalDateTime.parse(startDate.trim())
                : null;

        LocalDateTime end = (endDate != null && !endDate.isBlank())
                ? LocalDateTime.parse(endDate.trim())
                : null;

        // 빈 문자열일 경우 null 처리
        String filteredExerciseName = (exerciseName != null && !exerciseName.isBlank())
                ? exerciseName.trim()
                : null;

        List<String> filteredMuscleGroup = (muscleGroup != null && !muscleGroup.isEmpty())
                ? muscleGroup.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .toList()
                : null;

        // 서비스 호출
        Page<ExerciseRecordResponseDto> result = service.getRecords(
                auth.getName(),
                start,
                end,
                filteredExerciseName,
                filteredMuscleGroup,
                pageable
        );

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{recordId}/details")
    public ResponseEntity<List<ExerciseSetDetailDto>> getExerciseSetDetails(
            @PathVariable Long recordId,
            Authentication auth) {
        System.out.println("상세 조회 실행");
        List<ExerciseSetDetailDto> result = service.getExerciseSetDetails(recordId, auth.getName());
        return ResponseEntity.ok(result);
    }



//
//
//    @GetMapping("/stats/weekly-muscle-volume")
//    public ResponseEntity<List<WeeklyMuscleStatDto>> getWeeklyMuscleStats(
//            @RequestParam(defaultValue = "4") int weekCount,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        return ResponseEntity.ok(service.getWeeklyMuscleVolumeStats(userDetails.getUsername(), weekCount));
//    }
}
