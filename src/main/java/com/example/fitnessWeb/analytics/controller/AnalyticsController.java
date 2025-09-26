package com.example.fitnessWeb.analytics.controller;

import com.example.fitnessWeb.analytics.dto.ExerciseTypeVolumeDto;
import com.example.fitnessWeb.analytics.dto.MuscleBalanceResponse;
import com.example.fitnessWeb.analytics.dto.WeeklyVolumeResponseDto;
import com.example.fitnessWeb.analytics.projection.MaxRepsRow;
import com.example.fitnessWeb.analytics.projection.MaxWeightRepsRow;
import com.example.fitnessWeb.analytics.service.ExerciseAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ExerciseAnalyticsService analyticsService;

    // 주차별 볼륨
    @GetMapping("/weekly-volume/me")
    public ResponseEntity<List<WeeklyVolumeResponseDto>> weeklyVolumeMe(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<WeeklyVolumeResponseDto> dtoList =
                analyticsService.getWeeklyVolumesForRange(auth.getName(), start, end);
        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/max-reps-by-weight")
    public ResponseEntity<List<MaxRepsRow>> getRecentMaxRepsByWeight(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("exerciseTypeId") Long exerciseTypeId,
            @RequestParam("weight") Double weight,
            @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        List<MaxRepsRow> result = analyticsService.getRecentMaxRepsByWeight(
                user.getUsername(), exerciseTypeId, weight, size
        );
        return ResponseEntity.ok(result);
    }


    // 부위별 몰륨 밸런스
    @GetMapping("/muscle-balance")
    public MuscleBalanceResponse muscleBalance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return analyticsService.getMuscleBalance(userDetails.getUsername(), start, end);
    }


    /**
     * 운동기록 단위로 "최대 무게 & 그 무게에서 최대 reps"를 최근 N회 조회
     * 예) GET /analytics/max-weight-reps?exerciseTypeId=38&size=5
     */
    @GetMapping("/max-weight-reps")
    public ResponseEntity<List<MaxWeightRepsRow>> getMaxWeightRepsHistory(
            Authentication auth,
            @RequestParam("exerciseTypeId") Long exerciseTypeId,
            @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        List<MaxWeightRepsRow> result = analyticsService.getMaxWeightRepsHistory(
                auth.getName(), exerciseTypeId, size
        );
        return ResponseEntity.ok(result);
    }


    /**
     * 특정 사용자의 특정 운동 타입에 대한 기간 내 전체 볼륨과
     * 각 무게별 퍼센트 정보를 조회합니다.
     *
     * @param auth           인증 정보 (사용자 ID 추출)
     * @param exerciseTypeId 운동 타입 ID (필수)
     * @param start          조회 시작 날짜 (inclusive, nullable)
     * @param endExclusive   조회 종료 날짜 (exclusive, nullable)
     * @return 기간 내 운동 타입 전체 볼륨과 무게별 퍼센트 DTO
     */
    @GetMapping("/exercise-type-volume")
    public ResponseEntity<ExerciseTypeVolumeDto> getExerciseTypeVolume(
            Authentication auth,
            @RequestParam(name = "exerciseTypeId") Long exerciseTypeId,
            @RequestParam(name = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endExclusive
    ) {
        ExerciseTypeVolumeDto result = analyticsService.getExerciseTypeVolume(
                auth.getName(),
                exerciseTypeId,
                start,
                endExclusive
        );
        return ResponseEntity.ok(result);
    }
}
