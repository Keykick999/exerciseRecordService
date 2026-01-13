package com.example.fitnessWeb.exerciseRecord.controller;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;
import com.example.fitnessWeb.exerciseRecord.service.ExerciseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<String> save(@RequestBody @Valid List<ExerciseSetDto> dto,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        service.saveRecord(userDetails.getUsername(), dto);
        return ResponseEntity.ok("운동 기록 저장 완료");
    }


    // 운동 기록 조회
    @GetMapping
    public ResponseEntity<Slice<ExerciseRecordResponseDto>> getExerciseRecord(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String exerciseName,
            @RequestParam(required = false) List<String> muscleGroup,
            Authentication auth,
            Pageable pageable) {

        Slice<ExerciseRecordResponseDto> result = service.getRecords(
                auth.getName(),
                startDate,
                endDate,
                exerciseName,
                muscleGroup,
                pageable
        );

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{recordId}/details")
    public ResponseEntity<List<ExerciseSetDetailDto>> getExerciseSetDetails(
            @PathVariable Long recordId,
            Authentication auth) {
        List<ExerciseSetDetailDto> result = service.getExerciseSetDetails(recordId, auth.getName());
        return ResponseEntity.ok(result);
    }
}
