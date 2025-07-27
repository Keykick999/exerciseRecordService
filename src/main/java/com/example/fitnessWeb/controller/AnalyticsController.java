package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.WeeklyVolumeChangeDto;
import com.example.fitnessWeb.service.ExerciseAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
