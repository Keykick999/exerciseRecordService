package com.example.fitnessWeb.exerciseType.service;

import com.example.fitnessWeb.exerciseType.dto.ExerciseSaveDto;
import com.example.fitnessWeb.exerciseType.dto.ExerciseTypeResponseDto;
import com.example.fitnessWeb.common.exception.DuplicateExerciseTypeException;
import com.example.fitnessWeb.exerciseType.domain.ExerciseMuscle;
import com.example.fitnessWeb.exerciseType.domain.ExerciseType;
import com.example.fitnessWeb.exerciseType.domain.ExerciseTypeDocument;
import com.example.fitnessWeb.exerciseType.domain.Muscle;
import com.example.fitnessWeb.exerciseType.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.exerciseType.repository.ExerciseTypeSearchRepository;
import com.example.fitnessWeb.exerciseType.repository.MuscleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseTypeSearchRepository exerciseTypeSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final MuscleRepository muscleRepository;

    // ========================= 핵심 기능 (Controller에서 직접 쓰이는 메서드) =========================

    /**
     * 새로운 운동 생성
     */
    @Transactional
    public void createExerciseType(ExerciseSaveDto dto) {
        validateDuplicateExercise(dto.getName());
        List<Muscle> muscles = findValidMuscles(dto.getMuscleGroups());

        ExerciseType exerciseType = buildExerciseType(dto.getName(), muscles);
        exerciseTypeRepository.save(exerciseType);

        saveToElasticsearch(exerciseType, muscles);
    }

    /**
     * 운동 검색
     */
    public Page<ExerciseTypeResponseDto> searchExercise(String name, Set<String> muscleGroups, int page, int size) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExerciseTypeDocument> esResults = exerciseTypeSearchRepository.searchByNameAndMuscles(name, muscleGroups, pageable);

        return esResults.map(this::mapToResponseDto);
    }

    /**
     * 초기 기본 운동 DB 저장
     */
    @Transactional
    public String initializeDefaultExercises() {
        ensureBaseMusclesExist();

        List<ExerciseSaveDto> exercises = getDefaultExerciseDtos();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        exercises.forEach(dto -> {
            try {
                createExerciseType(dto);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                System.err.println("데이터 저장 실패: " + dto.getName() + ", 이유: " + e.getMessage());
            }
        });

        return String.format(
                "초기 데이터 DB 저장 완료: 총 %d건 시도, %d건 성공, %d건 실패 (중복 등)",
                exercises.size(),
                successCount.get(),
                failureCount.get()
        );
    }

    /**
     * MySQL 전체 데이터를 Elasticsearch에 동기화
     */
    @Transactional(readOnly = true)
    public void syncAllExerciseTypesToElasticsearch() {
        System.out.println("==> Elasticsearch 동기화 시작");

        int pageNum = 0;
        final int PAGE_SIZE = 10;
        Page<ExerciseType> exerciseIdPage;

        do {
            Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE);
            exerciseIdPage = exerciseTypeRepository.findAll(pageable);
            List<ExerciseType> exercisesOnPage = exerciseIdPage.getContent();

            if (exercisesOnPage.isEmpty()) break;

            List<Long> ids = exercisesOnPage.stream().map(ExerciseType::getId).toList();
            List<ExerciseType> fullExercises = exerciseTypeRepository.findWithMusclesByIdIn(ids);

            List<ExerciseTypeDocument> documents = fullExercises.stream()
                    .map(e -> ExerciseTypeDocument.builder()
                            .id(e.getId().toString())
                            .name(e.getName())
                            .muscleGroups(e.getExerciseMuscles().stream()
                                    .map(em -> em.getMuscle().getName())
                                    .distinct()
                                    .toList())
                            .build())
                    .toList();

            if (!documents.isEmpty()) exerciseTypeSearchRepository.saveAll(documents);

            pageNum++;
            try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        } while (exerciseIdPage.hasNext());

        IndexOperations indexOps = elasticsearchOperations.indexOps(ExerciseTypeDocument.class);
        indexOps.refresh();
        System.out.println("==> Elasticsearch 동기화 완료!");
    }

    // ========================= 헬퍼 메서드 =========================

    private void validateDuplicateExercise(String name) {
        if (exerciseTypeRepository.existsByName(name)) {
            throw new DuplicateExerciseTypeException("이미 존재하는 운동입니다.");
        }
    }

    private List<Muscle> findValidMuscles(List<String> requested) {
        List<Muscle> muscles = muscleRepository.findAllByNameIn(requested);
        Set<String> foundMuscleNames = muscles.stream().map(Muscle::getName).collect(Collectors.toSet());

        List<String> notFound = requested.stream()
                .filter(name -> !foundMuscleNames.contains(name))
                .toList();

        if (!notFound.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 부위입니다: " + notFound);
        }
        return muscles;
    }

    private ExerciseType buildExerciseType(String name, List<Muscle> muscles) {
        ExerciseType exerciseType = ExerciseType.builder().name(name).build();
        List<ExerciseMuscle> relations = muscles.stream()
                .map(m -> ExerciseMuscle.builder().exerciseType(exerciseType).muscle(m).build())
                .toList();
        exerciseType.setExerciseMuscles(relations);
        return exerciseType;
    }

    private void saveToElasticsearch(ExerciseType exerciseType, List<Muscle> muscles) {
        ExerciseTypeDocument document = ExerciseTypeDocument.builder()
                .id(String.valueOf(exerciseType.getId()))
                .name(exerciseType.getName())
                .muscleGroups(muscles.stream().map(Muscle::getName).distinct().toList())
                .build();
        exerciseTypeSearchRepository.save(document);
    }

    private ExerciseTypeResponseDto mapToResponseDto(ExerciseTypeDocument doc) {
        return ExerciseTypeResponseDto.builder()
                .id(Long.valueOf(doc.getId()))
                .name(doc.getName())
                .muscleGroups(doc.getMuscleGroups())
                .build();
    }

    private void ensureBaseMusclesExist() {
        List<String> baseMuscles = List.of("등", "가슴", "하체", "이두", "삼두", "어깨", "복근");
        baseMuscles.forEach(name ->
                muscleRepository.findByName(name).orElseGet(() -> muscleRepository.save(Muscle.builder().name(name).build()))
        );
    }

    private List<ExerciseSaveDto> getDefaultExerciseDtos() {
        List<ExerciseSaveDto> exercises = new ArrayList<>();

        // 가슴
        exercises.add(createDto("플랫 벤치프레스", List.of("가슴")));
        exercises.add(createDto("인클라인 벤치프레스", List.of("가슴")));
        exercises.add(createDto("디클라인 벤치프레스", List.of("가슴")));
        exercises.add(createDto("플랫 덤벨 프레스", List.of("가슴")));
        exercises.add(createDto("인클라인 덤벨 프레스", List.of("가슴")));
        exercises.add(createDto("딥스", List.of("가슴", "삼두")));
        exercises.add(createDto("푸쉬업", List.of("가슴")));
        exercises.add(createDto("케이블 크로스오버", List.of("가슴")));
        exercises.add(createDto("덤벨 플라이", List.of("가슴")));
        exercises.add(createDto("머신 체스트 프레스", List.of("가슴")));

        // 등
        exercises.add(createDto("컨벤셔널 데드리프트", List.of("등", "하체")));
        exercises.add(createDto("풀업", List.of("등")));
        exercises.add(createDto("랫풀다운", List.of("등")));
        exercises.add(createDto("바벨 로우", List.of("등")));
        exercises.add(createDto("덤벨 로우", List.of("등")));
        exercises.add(createDto("T바 로우", List.of("등")));
        exercises.add(createDto("시티드 케이블 로우", List.of("등")));
        exercises.add(createDto("백 익스텐션", List.of("등")));
        exercises.add(createDto("루마니안 데드리프트", List.of("하체", "등")));
        exercises.add(createDto("친업", List.of("등")));

        // 하체
        exercises.add(createDto("스쿼트", List.of("하체")));
        exercises.add(createDto("레그 프레스", List.of("하체")));
        exercises.add(createDto("런지", List.of("하체")));
        exercises.add(createDto("레그 익스텐션", List.of("하체")));
        exercises.add(createDto("레그 컬", List.of("하체")));
        exercises.add(createDto("스티프 레그 데드리프트", List.of("하체")));
        exercises.add(createDto("힙 쓰러스트", List.of("하체")));
        exercises.add(createDto("카프 레이즈", List.of("하체")));
        exercises.add(createDto("핵 스쿼트", List.of("하체")));
        exercises.add(createDto("고블릿 스쿼트", List.of("하체")));

        // 어깨
        exercises.add(createDto("오버헤드 프레스", List.of("어깨")));
        exercises.add(createDto("덤벨 숄더 프레스", List.of("어깨")));
        exercises.add(createDto("사이드 레터럴 레이즈", List.of("어깨")));
        exercises.add(createDto("프론트 레이즈", List.of("어깨")));
        exercises.add(createDto("벤트 오버 덤벨 레터럴 레이즈", List.of("어깨")));
        exercises.add(createDto("페이스풀", List.of("어깨", "등")));
        exercises.add(createDto("업라이트 로우", List.of("어깨")));
        exercises.add(createDto("아놀드 프레스", List.of("어깨")));
        exercises.add(createDto("머신 숄더 프레스", List.of("어깨")));
        exercises.add(createDto("슈러그", List.of("어깨")));

        // 삼두
        exercises.add(createDto("클로즈 그립 벤치프레스", List.of("삼두")));
        exercises.add(createDto("라잉 트라이셉스 익스텐션", List.of("삼두")));
        exercises.add(createDto("케이블 푸쉬다운", List.of("삼두")));
        exercises.add(createDto("오버헤드 덤벨 익스텐션", List.of("삼두")));
        exercises.add(createDto("케이블 오버헤드 익스텐션", List.of("삼두")));
        exercises.add(createDto("덤벨 킥백", List.of("삼두")));
        exercises.add(createDto("벤치 딥스", List.of("삼두")));
        exercises.add(createDto("다이아몬드 푸쉬업", List.of("삼두")));
        exercises.add(createDto("원 암 덤벨 오버헤드 익스텐션", List.of("삼두")));
        exercises.add(createDto("머신 트라이셉스 익스텐션", List.of("삼두")));

        // 이두
        exercises.add(createDto("바벨 컬", List.of("이두")));
        exercises.add(createDto("덤벨 컬", List.of("이두")));
        exercises.add(createDto("해머 컬", List.of("이두")));
        exercises.add(createDto("프리처 컬", List.of("이두")));
        exercises.add(createDto("인클라인 덤벨 컬", List.of("이두")));
        exercises.add(createDto("컨센트레이션 컬", List.of("이두")));
        exercises.add(createDto("케이블 컬", List.of("이두")));
        exercises.add(createDto("리버스 컬", List.of("이두")));
        exercises.add(createDto("드래그 컬", List.of("이두")));
        exercises.add(createDto("머신 바이셉스 컬", List.of("이두")));

        // 복근
        exercises.add(createDto("크런치", List.of("복근")));
        exercises.add(createDto("레그 레이즈", List.of("복근")));
        exercises.add(createDto("플랭크", List.of("복근")));
        exercises.add(createDto("행잉 레그 레이즈", List.of("복근")));
        exercises.add(createDto("러시안 트위스트", List.of("복근")));
        exercises.add(createDto("케이블 크런치", List.of("복근")));
        exercises.add(createDto("AB 롤아웃", List.of("복근")));
        exercises.add(createDto("바이시클 크런치", List.of("복근")));
        exercises.add(createDto("마운틴 클라이머", List.of("복근")));
        exercises.add(createDto("사이드 플랭크", List.of("복근")));

        return exercises;
    }

    private ExerciseSaveDto createDto(String name, List<String> muscleGroups) {
        ExerciseSaveDto dto = new ExerciseSaveDto();
        dto.setName(name);
        dto.setMuscleGroups(muscleGroups);
        return dto;
    }
}
