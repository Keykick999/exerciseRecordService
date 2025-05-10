package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ExerciseSaveDto;
import com.example.fitnessWeb.entity.ExerciseSearchTerm;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.repository.ExerciseSearchTermRepository;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseSearchTermRepository exerciseSearchTermRepository;

    public void createExerciseType(ExerciseSaveDto dto) {
        if (exerciseTypeRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 운동입니다.");
        }

        // ExerciseType 저장
        String name = dto.getName();
        ExerciseType exerciseType = ExerciseType.builder()
                .name(name)
                .muscleGroup(dto.getMuscleGroup())
                .build();
        exerciseTypeRepository.save(exerciseType);


        // 이름의 모든 부분 문자열 ExerciseSearchTerm 테이블에 저장
        name = name.replaceAll("\\s+", ""); // 공백 제거
        int length = name.length();

        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j <= length; j++) {
                String keyword = name.substring(0, i);
                ExerciseSearchTerm term = ExerciseSearchTerm.builder()
                        .keyword(keyword)
                        .exerciseType(exerciseType)
                        .build();
                exerciseSearchTermRepository.save(term);
            }
        }
    }

    public Page<ExerciseType> searchExercise(String name, HashSet<String> muscleGroups, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // TODO 정렬 로직 추가
        if (name == null) {
            if (muscleGroups == null) return exerciseTypeRepository.findAll(pageable);
            else return exerciseTypeRepository.findByMuscleGroups(muscleGroups, pageable);
        }
        else {
            // 이름을 키워드로 하여 매칭되는 종목의 id값 찾기
            List<ExerciseSearchTerm> exerciseTerms = exerciseSearchTermRepository.findByKeyword(name);

            // 각 id 저장하기
            HashSet<Long> exerciseIds = new HashSet<>();
            for (ExerciseSearchTerm exerciseTerm : exerciseTerms) {
                exerciseIds.add(exerciseTerm.getExerciseType().getId());
            }

            // id와 부위로 ExerciseType 조회
            if (muscleGroups == null) {
                return exerciseTypeRepository.findByIds(exerciseIds, pageable);
            }
            else {
                return exerciseTypeRepository.findByIdsAndMuscleGroups(exerciseIds, muscleGroups, pageable);
            }
        }
    }
}
