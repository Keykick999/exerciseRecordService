package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ExerciseSaveDto;
import com.example.fitnessWeb.entity.ExerciseSearchTerm;
import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.repository.ExerciseSearchTermRepository;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


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
        Set<String> uniqueKeywords = new HashSet<>();

        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j <= length; j++) {
                String keyword = name.substring(i, j);
                uniqueKeywords.add(keyword);
            }
        }

        List<ExerciseSearchTerm> terms = uniqueKeywords.stream()
                .map(keyword -> ExerciseSearchTerm.builder()
                        .keyword(keyword)
                        .exerciseType(exerciseType)
                        .build())
                .toList();


        // 한꺼번에 저장
        exerciseSearchTermRepository.saveAll(terms);
    }

    public Page<ExerciseType> searchExercise(String name, HashSet<String> muscleGroups, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // TODO 정렬 로직 추가
        System.out.println("name = " + name);
        System.out.println("name.isEmpty() = " + name.isEmpty());


        if (name.isEmpty()) {
            if (muscleGroups == null) {
                return exerciseTypeRepository.findAll(pageable);
            }
            else {
                return exerciseTypeRepository.findByMuscleGroups(muscleGroups, pageable);
            }
        }

        // name 기반 검색
        List<ExerciseType> foundExercises = exerciseSearchTermRepository.findExerciseTypesByKeyword(name);
        System.out.println("foundExercises = " + foundExercises);

        // muscleGroups 조건이 있을 경우 필터링
        List<ExerciseType> filtered = (muscleGroups == null)
                ? foundExercises
                : foundExercises.stream()
                        .filter(e -> muscleGroups.contains(e.getMuscleGroup()))
                        .toList();

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ExerciseType> pageContent = filtered.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, pageable, total);
    }
}
