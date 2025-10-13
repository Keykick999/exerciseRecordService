package com.example.fitnessWeb.goal.service;

import com.example.fitnessWeb.common.exception.UserNotFoundException;
import com.example.fitnessWeb.goal.dto.GoalResponseDto;
import com.example.fitnessWeb.goal.dto.WeeklySessionGoalRequestDto;
import com.example.fitnessWeb.goal.dto.WeeklySessionGoalUpdateDto;
import com.example.fitnessWeb.goal.entity.WeeklySessionGoal;
import com.example.fitnessWeb.goal.repository.WeeklySessionGoalRepository;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklySessionGoalService {

    private final WeeklySessionGoalRepository weeklySessionGoalRepository;
    private final UserRepository userRepository;

    // 생성
    public GoalResponseDto createGoal(String userId, WeeklySessionGoalRequestDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        WeeklySessionGoal goal = WeeklySessionGoal.builder()
                .user(user)
                .targetSessions(dto.getTargetSessions())
                .achievedSessions(0)
                .achieved(false)
                .build();

        weeklySessionGoalRepository.save(goal);

        return GoalResponseDto.builder()
                .id(goal.getId())
                .goalType("WEEKLY_SESSION")
                .details(goal)
                .build();
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<GoalResponseDto> getAllGoals(String userId) {
        return weeklySessionGoalRepository.findByUser_UserId(userId).stream()
                .map(goal -> GoalResponseDto.builder()
                        .id(goal.getId())
                        .goalType("WEEKLY_SESSION")
                        .details(goal)
                        .build())
                .toList();
    }

    // 단일 조회
    @Transactional(readOnly = true)
    public GoalResponseDto getGoalById(Long id) {
        WeeklySessionGoal goal = weeklySessionGoalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        return GoalResponseDto.builder()
                .id(goal.getId())
                .goalType("WEEKLY_SESSION")
                .details(goal)
                .build();
    }

    // 수정
    public GoalResponseDto updateGoal(Long id, WeeklySessionGoalUpdateDto dto) {
        WeeklySessionGoal goal = weeklySessionGoalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        goal = WeeklySessionGoal.builder()
                .id(goal.getId())
                .user(goal.getUser())
                .targetSessions(goal.getTargetSessions())
                .achievedSessions(dto.getAchievedSessions())
                .achieved(dto.isAchieved())
                .build();

        weeklySessionGoalRepository.save(goal);

        return GoalResponseDto.builder()
                .id(goal.getId())
                .goalType("WEEKLY_SESSION")
                .details(goal)
                .build();
    }

    // 삭제
    public void deleteGoal(Long id) {
        weeklySessionGoalRepository.deleteById(id);
    }
}
