package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.RoutineRequestDto;
import com.example.fitnessWeb.dto.RoutineSetDto;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.*;
import com.example.fitnessWeb.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class RoutineService {

    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final RoutineSetRepository routineSetRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    @Transactional
    public void saveRoutine(RoutineRequestDto requestDto) {
        // 1. 인증된 사용자 이름 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

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
            if (dto.getSetNumber() <= 0 || dto.getWeight() <= 0 || dto.getRepetitions() < 0) {
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
}
