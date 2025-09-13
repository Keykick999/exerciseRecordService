package com.example.fitnessWeb.service;

import com.example.fitnessWeb.dto.ExerciseSaveDto;
import com.example.fitnessWeb.dto.ExerciseTypeResponseDto;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.exception.DuplicateExerciseTypeException;
import com.example.fitnessWeb.repository.ExerciseTypeRepository;
import com.example.fitnessWeb.repository.ExerciseTypeSearchRepository;
import com.example.fitnessWeb.repository.MuscleRepository;
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
    @Transactional(readOnly = true)
    // @Transactional(readOnly = true) 어노테이션은 주석 처리하거나 제거합니다.
    public void syncAllExerciseTypesToElasticsearch() {
        System.out.println("==> Elasticsearch 동기화 (N+1 해결 버전)를 시작합니다...");

        // 인덱스 삭제 로직은 타임아웃을 유발할 수 있으므로 일단 생략합니다.
        // IndexOperations indexOps = ...

        int pageNum = 0;
        final int PAGE_SIZE = 10;
        Page<ExerciseType> exercisePage;

        System.out.println("DB에서 데이터를 페이지 단위(Fetch Join)로 조회하여 ES에 색인합니다...");

        do {
            Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE);

            // ▼▼▼ N+1 문제를 해결한 새 메서드 호출 ▼▼▼
            exercisePage = exerciseTypeRepository.findAllWithMusclesUsingFetchJoin(pageable);

            List<ExerciseType> exerciseTypesInPage = exercisePage.getContent();

            if (exerciseTypesInPage.isEmpty()) {
                if (pageNum == 0) System.out.println("DB에 동기화할 데이터가 없습니다.");
                else System.out.println("모든 페이지 처리를 완료했습니다.");
                break;
            }

            System.out.printf("==> %d 페이지 처리 중 (%d개 데이터 색인)%n", pageNum + 1, exerciseTypesInPage.size());

            List<ExerciseTypeDocument> documents = exerciseTypesInPage.stream()
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

            if (!documents.isEmpty()) {
                exerciseTypeSearchRepository.saveAll(documents);
            }

            pageNum++;

            // ▼▼▼ 서버 부하 감소를 위한 1초 대기 추가 ▼▼▼
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } while (exercisePage.hasNext());

        IndexOperations indexOps = elasticsearchOperations.indexOps(ExerciseTypeDocument.class);
        System.out.println("인덱스 refresh 중...");
        indexOps.refresh();
        System.out.println("==> Elasticsearch 동기화 완료!");
    }


    @Transactional
    public String initializeDefaultExercises() {
        // 1. 기본 근육들 먼저 보장
        List<String> baseMuscles = List.of("등", "가슴", "하체", "이두", "삼두", "어깨", "복근");
        baseMuscles.forEach(name -> {
            muscleRepository.findByName(name).orElseGet(() ->
                    muscleRepository.save(Muscle.builder().name(name).build())
            );
        });


        List<ExerciseSaveDto> exercises = List.of(
                // 가슴
                createDto("플랫 벤치프레스", List.of("가슴")),
                createDto("인클라인 벤치프레스", List.of("가슴")),
                createDto("디클라인 벤치프레스", List.of("가슴")),
                createDto("플랫 덤벨 프레스", List.of("가슴")),
                createDto("인클라인 덤벨 프레스", List.of("가슴")),
                createDto("딥스", List.of("가슴", "삼두")),
                createDto("푸쉬업", List.of("가슴")),
                createDto("케이블 크로스오버", List.of("가슴")),
                createDto("덤벨 플라이", List.of("가슴")),
                createDto("머신 체스트 프레스", List.of("가슴")),
                // 등
                createDto("컨벤셔널 데드리프트", List.of("등", "하체")),
                createDto("풀업", List.of("등")),
                createDto("랫풀다운", List.of("등")),
                createDto("바벨 로우", List.of("등")),
                createDto("덤벨 로우", List.of("등")),
                createDto("T바 로우", List.of("등")),
                createDto("시티드 케이블 로우", List.of("등")),
                createDto("백 익스텐션", List.of("등")),
                createDto("루마니안 데드리프트", List.of("하체", "등")),
                createDto("친업", List.of("등")),
                // 하체
                createDto("스쿼트", List.of("하체")),
                createDto("레그 프레스", List.of("하체")),
                createDto("런지", List.of("하체")),
                createDto("레그 익스텐션", List.of("하체")),
                createDto("레그 컬", List.of("하체")),
                createDto("스티프 레그 데드리프트", List.of("하체")),
                createDto("힙 쓰러스트", List.of("하체")),
                createDto("카프 레이즈", List.of("하체")),
                createDto("핵 스쿼트", List.of("하체")),
                createDto("고블릿 스쿼트", List.of("하체")),
                // 어깨
                createDto("오버헤드 프레스", List.of("어깨")),
                createDto("덤벨 숄더 프레스", List.of("어깨")),
                createDto("사이드 레터럴 레이즈", List.of("어깨")),
                createDto("프론트 레이즈", List.of("어깨")),
                createDto("벤트 오버 덤벨 레터럴 레이즈", List.of("어깨")),
                createDto("페이스풀", List.of("어깨", "등")),
                createDto("업라이트 로우", List.of("어깨")),
                createDto("아놀드 프레스", List.of("어깨")),
                createDto("머신 숄더 프레스", List.of("어깨")),
                createDto("슈러그", List.of("어깨")),
                // 삼두
                createDto("클로즈 그립 벤치프레스", List.of("삼두")),
                createDto("라잉 트라이셉스 익스텐션", List.of("삼두")),
                createDto("케이블 푸쉬다운", List.of("삼두")),
                createDto("오버헤드 덤벨 익스텐션", List.of("삼두")),
                createDto("케이블 오버헤드 익스텐션", List.of("삼두")),
                createDto("덤벨 킥백", List.of("삼두")),
                createDto("벤치 딥스", List.of("삼두")),
                createDto("다이아몬드 푸쉬업", List.of("삼두")),
                createDto("원 암 덤벨 오버헤드 익스텐션", List.of("삼두")),
                createDto("머신 트라이셉스 익스텐션", List.of("삼두")),
                // 이두
                createDto("바벨 컬", List.of("이두")),
                createDto("덤벨 컬", List.of("이두")),
                createDto("해머 컬", List.of("이두")),
                createDto("프리처 컬", List.of("이두")),
                createDto("인클라인 덤벨 컬", List.of("이두")),
                createDto("컨센트레이션 컬", List.of("이두")),
                createDto("케이블 컬", List.of("이두")),
                createDto("리버스 컬", List.of("이두")),
                createDto("드래그 컬", List.of("이두")),
                createDto("머신 바이셉스 컬", List.of("이두")),
                // 복근
                createDto("크런치", List.of("복근")),
                createDto("레그 레이즈", List.of("복근")),
                createDto("플랭크", List.of("복근")),
                createDto("행잉 레그 레이즈", List.of("복근")),
                createDto("러시안 트위스트", List.of("복근")),
                createDto("케이블 크런치", List.of("복근")),
                createDto("AB 롤아웃", List.of("복근")),
                createDto("바이시클 크런치", List.of("복근")),
                createDto("마운틴 클라이머", List.of("복근")),
                createDto("사이드 플랭크", List.of("복근"))
        );

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        exercises.forEach(dto -> {
            try {
                this.createExerciseType(dto); // Service 내의 메서드 호출
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
     * ExerciseSaveDto 생성을 위한 헬퍼 메서드
     */
    private ExerciseSaveDto createDto(String name, List<String> muscleGroups) {
        ExerciseSaveDto dto = new ExerciseSaveDto();
        dto.setName(name);
        dto.setMuscleGroups(muscleGroups);
        return dto;
    }
}