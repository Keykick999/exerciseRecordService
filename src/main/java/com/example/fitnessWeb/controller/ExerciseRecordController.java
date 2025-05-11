package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.ExerciseRecordDto;
import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseRecordSearchRequest;
import com.example.fitnessWeb.service.ExerciseRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {

    private final ExerciseRecordService service;


    // 운동 기록 저장
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid ExerciseRecordDto dto,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        service.saveRecord(userDetails.getUsername(), dto);
        return ResponseEntity.ok("운동 기록 저장 완료");
    }


    // 운동 기록 조회
    @GetMapping
    public ResponseEntity<List<ExerciseRecordResponseDto>> getExerciseRecord(
            @RequestBody ExerciseRecordSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ExerciseRecordResponseDto> result = service.getRecords(userDetails.getUsername(), request);
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
