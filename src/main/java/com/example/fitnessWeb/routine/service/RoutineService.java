package com.example.fitnessWeb.routine.service;

import com.example.fitnessWeb.common.exception.*;
import com.example.fitnessWeb.exerciseType.domain.ExerciseType;
import com.example.fitnessWeb.exerciseType.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.routine.domain.Routine;
import com.example.fitnessWeb.routine.domain.RoutineSet;
import com.example.fitnessWeb.routine.dto.*;
import com.example.fitnessWeb.routine.repository.RoutineRepository;
import com.example.fitnessWeb.routine.repository.RoutineSetRepository;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineSetRepository routineSetRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final AuthService userService;

    // 루틴 저장
    @Transactional
    public void saveRoutine(String userId, RoutineRequestDto requestDto) {
        User user = userService.findUserOrThrow(userId);
        validateRoutineNameNotDuplicate(userId, requestDto.getName());

        Routine routine = createRoutine(user, requestDto.getName());
        routineRepository.save(routine);

        saveRoutineSets(routine, requestDto.getRoutineSets());
    }

    // 루틴 목록 조회
    public Page<RoutineSummaryDto> getMyRoutines(String userId, int page, int size) {
        userService.findUserOrThrow(userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<RoutineSummaryDto> routines = routineRepository.findAllByUserId(userId, pageable);

        if (routines.isEmpty()) {
            throw new RoutineNotFoundException();
        }

        return routines;
    }

    // 루틴 상세 조회
    public List<RoutineDetailDto> getRoutineDetails(String userId, String routineName) {
        userService.findUserOrThrow(userId);

        List<RoutineDetailDto> result = routineRepository.findRoutineDetailsByNameAndUserId(routineName, userId);

        if (result.isEmpty()) {
            throw new RoutineNotFoundException();
        }

        return result;
    }

    // 루틴 수정
    @Transactional
    public void updateRoutine(String userId, Long routineId, RoutineUpdateRequestDto requestDto) {
        Routine routine = findRoutineOrThrow(routineId);
        validateRoutineOwner(userId, routine);

        routine.getRoutineSets().clear();

        List<RoutineSet> newRoutineSets = createRoutineSets(routine, requestDto.getRoutineSets());
        routine.setName(requestDto.getRoutineName());
        routine.getRoutineSets().addAll(newRoutineSets);
    }

    // 루틴 삭제
    @Transactional
    public void deleteRoutine(String userId, Long routineId) {
        Routine routine = findRoutineOrThrow(routineId);
        validateRoutineOwner(userId, routine);

        routineRepository.delete(routine);
    }

    /* ===================== Helper Methods ===================== */

    private void validateRoutineNameNotDuplicate(String userId, String routineName) {
        boolean exists = routineRepository.existsByNameAndUserUserId(routineName, userId);
        if (exists) {
            throw new DuplicateRoutineNameException(routineName);
        }
    }

    private Routine createRoutine(User user, String routineName) {
        return Routine.builder()
                .name(routineName)
                .user(user)
                .build();
    }

    private void saveRoutineSets(Routine routine, List<RoutineSetDto> routineSetDtos) {
        Set<String> duplicateCheck = new HashSet<>();

        for (RoutineSetDto dto : routineSetDtos) {
            validateRoutineSetDto(dto);
            checkDuplicateRoutineSet(dto, duplicateCheck);

            ExerciseType exerciseType = exerciseTypeRepository.findById(dto.getExerciseTypeId())
                    .orElseThrow(() -> new ExerciseTypeNotFoundException(dto.getExerciseTypeId()));

            RoutineSet set = RoutineSet.builder()
                    .routine(routine)
                    .exerciseType(exerciseType)
                    .setNumber(dto.getSetNumber())
                    .weight(dto.getWeight())
                    .repetitions(dto.getRepetitions())
                    .build();

            routineSetRepository.save(set);
        }
    }

    private void validateRoutineSetDto(RoutineSetDto dto) {
        if (dto.getSetNumber() <= 0 || dto.getWeight() < 0 || dto.getRepetitions() < 0) {
            throw new InvalidRoutineSetException("세트번호, 중량, 횟수는 양의 정수여야 합니다.");
        }
    }

    private void checkDuplicateRoutineSet(RoutineSetDto dto, Set<String> duplicateCheck) {
        String key = dto.getExerciseTypeId() + "-" + dto.getSetNumber();
        if (!duplicateCheck.add(key)) {
            throw new DuplicateRoutineSetException(dto.getExerciseTypeId(), dto.getSetNumber());
        }
    }

    private Routine findRoutineOrThrow(Long routineId) {
        return routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다. ID: " + routineId));
    }

    private void validateRoutineOwner(String userId, Routine routine) {
        if (!routine.getUser().getUserId().equals(userId)) {
            throw new UnAuthorizedAccessException("본인의 루틴만 접근할 수 있습니다.");
        }
    }

    private List<RoutineSet> createRoutineSets(Routine routine, List<RoutineSetDto> dtoList) {
        return dtoList.stream()
                .map(dto -> RoutineSet.builder()
                        .routine(routine)
                        .exerciseType(exerciseTypeRepository.findById(dto.getExerciseTypeId())
                                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 운동 ID: " + dto.getExerciseTypeId())))
                        .setNumber(dto.getSetNumber())
                        .weight(dto.getWeight())
                        .repetitions(dto.getRepetitions())
                        .build())
                .toList();
    }
}
