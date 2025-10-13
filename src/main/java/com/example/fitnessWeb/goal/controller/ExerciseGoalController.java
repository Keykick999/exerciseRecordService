package com.example.fitnessWeb.goal.controller;

import com.example.fitnessWeb.goal.dto.ExerciseGoalRequestDto;
import com.example.fitnessWeb.goal.dto.ExerciseGoalResponseDto;
import com.example.fitnessWeb.goal.service.ExerciseGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals/exercise")
public class ExerciseGoalController {

    private final ExerciseGoalService exerciseGoalService;

    @PostMapping
    public ExerciseGoalResponseDto createGoal(
            Authentication auth,
            @RequestBody ExerciseGoalRequestDto dto) {
        return exerciseGoalService.createGoal(auth.getName(), dto);
    }

    @GetMapping
    public List<ExerciseGoalResponseDto> getGoals(Authentication auth) {
        return exerciseGoalService.getGoals(auth.getName());
    }
}