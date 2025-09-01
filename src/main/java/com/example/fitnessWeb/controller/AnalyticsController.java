package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.MuscleBalanceResponse;
import com.example.fitnessWeb.dto.WeeklyVolumeChangeDto;
import com.example.fitnessWeb.service.ExerciseAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    @GetMapping("/weekly-volume")
    public List<WeeklyVolumeChangeDto> weeklyVolume() {
        return analyticsService.getWeeklyVolumeChanges();
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
}
