package com.example.fitnessWeb.goal.controller;

import com.example.fitnessWeb.goal.dto.GoalResponseDto;
import com.example.fitnessWeb.goal.dto.WeeklySessionGoalRequestDto;
import com.example.fitnessWeb.goal.dto.WeeklySessionGoalUpdateDto;
import com.example.fitnessWeb.goal.service.WeeklySessionGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals/weekly-session")
public class WeeklySessionGoalController {

    private final WeeklySessionGoalService weeklySessionGoalService;

    // 생성
    @PostMapping
    public GoalResponseDto createGoal(Authentication auth, @RequestBody WeeklySessionGoalRequestDto dto) {
        return weeklySessionGoalService.createGoal(auth.getName(), dto);
    }

    // 전체 조회
    @GetMapping
    public List<GoalResponseDto> getAllGoals(Authentication auth) {
        return weeklySessionGoalService.getAllGoals(auth.getName());
    }

    // 단일 조회
    @GetMapping("/{id}")
    public GoalResponseDto getGoalById(@PathVariable Long id) {
        return weeklySessionGoalService.getGoalById(id);
    }

    // 수정
    @PutMapping("/{id}")
    public GoalResponseDto updateGoal(@PathVariable Long id, @RequestBody WeeklySessionGoalUpdateDto dto) {
        return weeklySessionGoalService.updateGoal(id, dto);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void deleteGoal(@PathVariable Long id) {
        weeklySessionGoalService.deleteGoal(id);
    }
}
