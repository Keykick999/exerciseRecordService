package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ExerciseSaveDto;
import com.example.fitnessWeb.dto.ExerciseTypeProjection;
import com.example.fitnessWeb.dto.ExerciseTypeResponseDto;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.DuplicateExerciseTypeException;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.repository.ExerciseTypeSearchRepository;
import com.example.fitnessWeb.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseTypeSearchRepository exerciseTypeSearchRepository;
    private final MuscleRepository muscleRepository;

    public void createExerciseType(ExerciseSaveDto dto) {
        if (exerciseTypeRepository.existsByName(dto.getName())) {
            throw new DuplicateExerciseTypeException("이미 존재하는 운동입니다.");
        }

        // Muscle 조회 및 유효성 검사
        List<String> requested = dto.getMuscleGroups();
        List<Muscle> muscles = muscleRepository.findAllByNameIn(requested);

        Set<String> foundMuscleNames = muscles.stream()
                .map(Muscle::getName)
                .collect(Collectors.toSet());

        List<String> notFound = requested.stream()
                .filter(name -> !foundMuscleNames.contains(name))
                .toList();

        if (!notFound.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 부위입니다: " + notFound);
        }

        // ExerciseType 생성
        ExerciseType exerciseType = ExerciseType.builder()
                .name(dto.getName())
                .build();

        List<ExerciseMuscle> relations = muscles.stream()
                .map(muscle -> ExerciseMuscle.builder()
                        .exerciseType(exerciseType)
                        .muscle(muscle)
                        .build())
                .toList();

        exerciseType.setExerciseMuscles(relations);
        exerciseTypeRepository.save(exerciseType);


        // ElasticSearch에 저장
        ExerciseTypeDocument document = ExerciseTypeDocument.builder()
                .id(String.valueOf(exerciseType.getId()))
                .name(exerciseType.getName())
                .muscleGroups(
                        muscles.stream()
                                .map(Muscle::getName)
                                .distinct()
                                .toList()
                )
                .build();

        exerciseTypeSearchRepository.save(document);
    }



    public Page<ExerciseTypeResponseDto> searchExercise(String name, Set<String> muscleGroups, int page, int size) throws IOException {
        Pageable pageable = PageRequest.of(page, size);

        Page<ExerciseTypeDocument> esResults = exerciseTypeSearchRepository.searchByNameAndMuscles(name, muscleGroups, pageable);

        return esResults.map(doc -> ExerciseTypeResponseDto.builder()
                .id(Long.valueOf(doc.getId()))
                .name(doc.getName())
                .muscleGroups(doc.getMuscleGroups())
                .build());
    }


    // 운동 검색
//    public Page<ExerciseType> searchExercise(String name, HashSet<String> muscleGroups, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//
//        // 1. 검색어가 없으면 muscleGroup 기준 MySQL 검색
//        if (name == null || name.isBlank()) {
//            if (muscleGroups == null || muscleGroups.isEmpty()) {
//                return exerciseTypeRepository.findAll(pageable);
//            } else {
//                return exerciseTypeRepository.findByMuscleGroups(muscleGroups, pageable);
//            }
//        }
//
//        // 2. 검색어가 있으면 Elasticsearch에서 이름 검색
//        List<ExerciseTypeDocument> esResults = exerciseTypeSearchRepository.findByNameContaining(name);
//
//        // 3. muscleGroup 필터링 적용
//        List<ExerciseTypeDocument> filtered = (muscleGroups == null || muscleGroups.isEmpty())
//                ? esResults
//                : esResults.stream()
//                .filter(d -> muscleGroups.contains(d.getMuscleGroup()))
//                .toList();
//
//        // 4. ES Document → JPA Entity로 매핑
//        List<ExerciseType> entities = filtered.stream()
//                .map(doc -> ExerciseType.builder()
//                        .id(Long.valueOf(doc.getId()))
//                        .name(doc.getName())
//                        .muscleGroup(doc.getMuscleGroup())
//                        .build())
//                .toList();
//
//        // 5. 페이징 수동 처리
//        int total = entities.size();
//        int fromIndex = Math.min(page * size, total);
//        int toIndex = Math.min(fromIndex + size, total);
//        List<ExerciseType> pageContent = entities.subList(fromIndex, toIndex);
//
//        return new PageImpl<>(pageContent, pageable, total);
//    }



    // 기존 MySQL -> ElasticSearch 동기화 메서드
    public void syncAllExerciseTypesToElasticsearch() {
        List<ExerciseType> exerciseTypes = exerciseTypeRepository.findAllWithMuscles();

        List<ExerciseTypeDocument> documents = exerciseTypes.stream()
                .map(e -> ExerciseTypeDocument.builder()
                        .id(e.getId().toString())
                        .name(e.getName())
                        .muscleGroups(
                                e.getExerciseMuscles().stream()
                                        .map(em -> em.getMuscle().getName())
                                        .distinct()
                                        .toList()
                        )
                        .build())
                .toList();

        exerciseTypeSearchRepository.saveAll(documents);
    }
}



















