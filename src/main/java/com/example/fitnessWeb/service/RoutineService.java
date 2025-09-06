package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.*;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.*;
import com.example.fitnessWeb.repository.*;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RoutineService {

    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final RoutineSetRepository routineSetRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;


    // 루틴 저장
    @Transactional
    public void saveRoutine(String userId, RoutineRequestDto requestDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 루틴 이름 중복 체크
        boolean exists = routineRepository.existsByNameAndUserUserId(requestDto.getName(), userId);
        if (exists) {
            throw new DuplicateRoutineNameException(requestDto.getName());
        }

        // 3. 루틴 저장
        Routine routine = Routine.builder()
                .name(requestDto.getName())
                .user(user)
                .build();
        routineRepository.save(routine);

        // 4. 루틴 세트 저장
        Set<String> duplicateCheck = new HashSet<>();
        for (RoutineSetDto dto : requestDto.getRoutineSets()) {
            // 유효성 검사
            if (dto.getSetNumber() <= 0 || dto.getWeight() < 0 || dto.getRepetitions() < 0) {
                throw new InvalidRoutineSetException("세트번호, 중량, 횟수는 양의 정수여야 합니다.");
            }

            // 중복 체크: 같은 운동(exerciseTypeId)에서 같은 setNumber가 들어왔는지 확인
            String key = dto.getExerciseTypeId() + "-" + dto.getSetNumber();
            if (!duplicateCheck.add(key)) {
                throw new DuplicateRoutineSetException(dto.getExerciseTypeId(), dto.getSetNumber());
            }


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


    // 루틴 목록 조회
    public Page<RoutineSummaryDto> getMyRoutines(String userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Pageable pageable = PageRequest.of(page, size);
        Page<RoutineSummaryDto> routines = routineRepository.findAllByUserId(userId, pageable);
        if (routines.isEmpty()) {
            throw new RoutineNotFoundException();
        }

        return routines;
    }


    // 루틴 상세 정보 조회
    public List<RoutineDetailDto> getRoutineDetails(String userId, String routineName) {
        userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        List<RoutineDetailDto> result = routineRepository.findRoutineDetailsByNameAndUserId(routineName, userId);

        if (result.isEmpty()) {
            throw new RoutineNotFoundException();
        }

        return result;
    }


    // 루틴 수정
    @Transactional
    public void updateRoutine(String userId, Long routineId, RoutineUpdateRequestDto requestDto) {
        // 1. 루틴 조회
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다. ID: " + routineId));

        // 3. 소유자 검사
        if (!routine.getUser().getUserId().equals(userId)) {
            throw new UnAuthorizedAccessException("본인의 루틴만 수정할 수 있습니다.");
        }

        // 4. 기존 루틴 세트 삭제
        routine.getRoutineSets().clear();

        // 5. 새로운 루틴 세트 생성
        List<RoutineSet> newRoutineSets = requestDto.getRoutineSets().stream()
                .map(dto -> RoutineSet.builder()
                        .routine(routine)
                        .exerciseType(exerciseTypeRepository.findById(dto.getExerciseTypeId())
                                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 운동 ID: " + dto.getExerciseTypeId())))
                        .setNumber(dto.getSetNumber())
                        .weight(dto.getWeight())
                        .repetitions(dto.getRepetitions())
                        .build())
                .toList();

        // 6. 루틴 이름 및 루틴 세트 업데이트
        routine.setName(requestDto.getRoutineName());
        routine.getRoutineSets().addAll(newRoutineSets);
    }


    // 루틴 삭제
    @Transactional
    public void deleteRoutine(String userId, Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));

        if (!routine.getUser().getUserId().equals(userId)) {
            throw new UnAuthorizedAccessException("본인의 루틴만 삭제할 수 있습니다.");
        }

        routineRepository.delete(routine); // cascade + orphanRemoval 설정되어 있다면 routineSets도 함께 삭제됨
    }
}