package com.example.fitnessWeb.goal.service;

import com.example.fitnessWeb.common.exception.ExerciseTypeNotFoundException;
import com.example.fitnessWeb.common.exception.UserNotFoundException;
import com.example.fitnessWeb.exerciseType.domain.ExerciseType;
import com.example.fitnessWeb.exerciseType.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.goal.dto.ExerciseGoalRequestDto;
import com.example.fitnessWeb.goal.dto.ExerciseGoalResponseDto;
import com.example.fitnessWeb.goal.entity.ExerciseGoal;
import com.example.fitnessWeb.goal.repository.ExerciseGoalRepository;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseGoalService {

    private final ExerciseGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    @Transactional
    public ExerciseGoalResponseDto createGoal(String userId, ExerciseGoalRequestDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        ExerciseType exerciseType = exerciseTypeRepository.findById(dto.getExerciseTypeId())
                .orElseThrow(() -> new ExerciseTypeNotFoundException(dto.getExerciseTypeId()));

        ExerciseGoal goal = ExerciseGoal.builder()
                .exerciseType(exerciseType)
                .user(user)
                .weight(dto.getWeight())
                .reps(dto.getReps())
                .achieved(false)
                .build();

        goalRepository.save(goal);
        return ExerciseGoalResponseDto.fromEntity(goal);
    }

    @Transactional(readOnly = true)
    public List<ExerciseGoalResponseDto> getGoals(String userId) {
        return goalRepository.findByUser_UserId(userId)
                .stream()
                .map(ExerciseGoalResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
