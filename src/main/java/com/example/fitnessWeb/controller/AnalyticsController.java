package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.MuscleBalanceResponse;
import com.example.fitnessWeb.dto.WeeklyVolumeResponseDto;
import com.example.fitnessWeb.projection.MaxRepsRow;
import com.example.fitnessWeb.projection.MaxVolumeSetRow;
import com.example.fitnessWeb.projection.MaxWeightRepsRow;
import com.example.fitnessWeb.service.ExerciseAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    // 이번주 볼륨
    @GetMapping("/weekly-volume/me")
    public ResponseEntity<WeeklyVolumeResponseDto> weeklyVolumeMe(
            @AuthenticationPrincipal UserDetails user, // 인증 사용자
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WeeklyVolumeResponseDto dto =
                analyticsService.getWeeklyVolumeFor(user.getUsername(), date);
        return ResponseEntity.ok(dto);
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
        System.out.println("userDetails.getUsername() = " + userDetails.getUsername());
        return analyticsService.getMuscleBalance(userDetails.getUsername(), start, end);
    }


    @GetMapping("/max-reps-by-weight")
    public ResponseEntity<List<MaxRepsRow>> getRecentMaxRepsByWeight(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("exerciseTypeId") Long exerciseTypeId,
            @RequestParam("weight") Integer weight,
            @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        List<MaxRepsRow> result = analyticsService.getRecentMaxRepsByWeight(
                user.getUsername(), exerciseTypeId, weight, size
        );
        return ResponseEntity.ok(result);
    }


    /**
     * 운동기록 단위로 "최대 무게 & 그 무게에서 최대 reps"를 최근 N회 조회
     * 예) GET /analytics/max-weight-reps?exerciseTypeId=38&size=5
     */
    @GetMapping("/max-weight-reps")
    public ResponseEntity<List<MaxWeightRepsRow>> getMaxWeightRepsHistory(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("exerciseTypeId") Long exerciseTypeId,
            @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        List<MaxWeightRepsRow> result = analyticsService.getMaxWeightRepsHistory(
                user.getUsername(), exerciseTypeId, size
        );
        return ResponseEntity.ok(result);
    }


    /**
     * 운동기록 단위로 "최대 볼륨 세트" 조회 (옵션: 종목/기간)
     * 예) GET /analytics/max-volume-sets?exerciseTypeId=38&size=5g
     *    GET /analytics/max-volume-sets?size=10&start=2025-08-01&end=2025-09-01
     */
    @GetMapping("/max-volume-sets")
    public ResponseEntity<List<MaxVolumeSetRow>> getMaxVolumeSets(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(name = "exerciseTypeId", required = false) Long exerciseTypeId,
            @RequestParam(name = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(name = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endExclusive,
            @RequestParam(name = "size", defaultValue = "5") Integer size
    ) {
        List<MaxVolumeSetRow> result = analyticsService.getMaxVolumeSets(
                user.getUsername(),
                exerciseTypeId,
                start,
                endExclusive,
                size
        );
        return ResponseEntity.ok(result);
    }
}
