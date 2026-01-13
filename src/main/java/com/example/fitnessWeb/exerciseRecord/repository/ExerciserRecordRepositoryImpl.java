package com.example.fitnessWeb.exerciseRecord.repository;

import com.example.fitnessWeb.exerciseRecord.domain.QExerciseRecord;
import com.example.fitnessWeb.exerciseRecord.domain.QExerciseSet;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.exerciseType.domain.QExerciseMuscle;
import com.example.fitnessWeb.exerciseType.domain.QExerciseType;
import com.example.fitnessWeb.exerciseType.domain.QMuscle;
import com.example.fitnessWeb.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExerciserRecordRepositoryImpl implements ExerciseRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExerciseRecordResponseDto> searchRecords(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String exerciseName,
            List<String> muscleGroupList,
            Pageable pageable) {

        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        QExerciseSet set = QExerciseSet.exerciseSet;
        QExerciseType type = QExerciseType.exerciseType;
        QExerciseMuscle em = QExerciseMuscle.exerciseMuscle;
        QMuscle m = QMuscle.muscle;

        // =================================================================================
        // STEP 1. 페이징 쿼리 (가장 중요)
        // -> 조건에 맞는 ID와 CreatedAt만 먼저 가져옴 (커버링 인덱스 효과)
        // =================================================================================
        JPAQuery<ExerciseRecordResponseDto> paginationQuery = queryFactory
                .select(Projections.constructor(ExerciseRecordResponseDto.class,
                        record.id,
                        record.createdAt
                ))
                .from(record);

        // [최적화] 필터 조건이 있을 때만 조인 수행 (없으면 조인 안 함 -> 인덱스만 탐)
        boolean needJoin = StringUtils.hasText(exerciseName) || (muscleGroupList != null && !muscleGroupList.isEmpty());

        if (needJoin) {
            paginationQuery
                    .join(record.sets, set)
                    .join(set.exerciseType, type)
                    .join(type.exerciseMuscles, em)
                    .join(em.muscle, m);
        }

        // 조건 적용
        paginationQuery.where(
                userIdEq(userId),
                dateBetween(startDate, endDate),
                exerciseNameEq(exerciseName),
                muscleGroupIn(muscleGroupList)
        );

        // 페이징 적용 (distinct()는 조인 시 중복 제거를 위해 필요)
        List<ExerciseRecordResponseDto> content = paginationQuery
                .orderBy(record.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct() // 1:N 조인 시 뻥튀기 방지 (ID 기준)
                .fetch();

        // 조회된 결과가 없으면 바로 빈 페이지 반환 (불필요한 추가 쿼리 방지)
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // =================================================================================
        // STEP 2. 근육 정보 조회 (IN 절 활용)
        // -> 위에서 찾은 20개의 ID에 대해서만 근육 정보를 가져옴 (배치 사이즈 효과)
        // =================================================================================
        List<Long> recordIds = content.stream()
                .map(ExerciseRecordResponseDto::getExerciseRecordId)
                .toList();

        // 20개에 대한 근육 정보만 싹 긁어옴
        List<Tuple> muscleData = queryFactory
                .select(record.id, m.name)
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(record.id.in(recordIds)) // 👈 핵심: ID 20개만 검색
                .distinct()
                .fetch();

        // =================================================================================
        // STEP 3. 메모리에서 조립 (Java가 DB보다 잘함)
        // =================================================================================
        // Map<RecordId, "가슴, 등"> 형태로 변환
        Map<Long, String> muscleMap = muscleData.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(record.id),
                        Collectors.mapping(tuple -> tuple.get(m.name), Collectors.joining(", "))
                ));

        // 결과 DTO에 근육 문자열 채워 넣기
        content.forEach(dto -> {
            String muscles = muscleMap.getOrDefault(dto.getExerciseRecordId(), "");
            // DTO에 setMuscleGroups 메서드가 없으면 생성자나 필드로 처리해야 함
            // 여기서는 DTO 구조상 새로운 객체를 만들거나 setter 사용 (아래 설명 참조)
            dto.setMuscleGroups(muscles); // DTO에 @Setter가 있거나 메서드 추가 필요
        });

        // =================================================================================
        // STEP 4. Total Count 쿼리
        // =================================================================================
        JPAQuery<Long> countQuery = queryFactory
                .select(record.id.countDistinct())
                .from(record);

        if (needJoin) {
            countQuery
                    .join(record.sets, set)
                    .join(set.exerciseType, type)
                    .join(type.exerciseMuscles, em)
                    .join(em.muscle, m);
        }

        Long total = countQuery
                .where(
                        userIdEq(userId),
                        dateBetween(startDate, endDate),
                        exerciseNameEq(exerciseName),
                        muscleGroupIn(muscleGroupList)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // --- 조건 메서드 분리 (가독성 향상) ---

    private BooleanExpression userIdEq(String userId) {
        return QExerciseRecord.exerciseRecord.user.userId.eq(userId);
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        if (startDate == null && endDate == null) return null;
        if (startDate != null && endDate != null) return record.createdAt.between(startDate, endDate);
        if (startDate != null) return record.createdAt.goe(startDate);
        return record.createdAt.loe(endDate);
    }

    private BooleanExpression exerciseNameEq(String exerciseName) {
        return StringUtils.hasText(exerciseName) ? QExerciseType.exerciseType.name.eq(exerciseName) : null;
    }

    private BooleanExpression muscleGroupIn(List<String> muscleGroupList) {
        return (muscleGroupList != null && !muscleGroupList.isEmpty()) ? QMuscle.muscle.name.in(muscleGroupList) : null;
    }


    @Override
    public List<ExerciseSetDetailDto> findExerciseSetDetails(Long recordId, String userId) {
        QExerciseSet set = QExerciseSet.exerciseSet;
        QExerciseType type = QExerciseType.exerciseType;
        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        QUser user = QUser.user;

        JPAQuery<ExerciseSetDetailDto> query = queryFactory
                .select(Projections.constructor(
                        ExerciseSetDetailDto.class,
                        type.name,
                        set.setOrder,
                        set.weight,
                        set.reps
                ))
                .from(set)
                .join(set.exerciseType, type)
                .join(record).on(set.exerciseRecord.id.eq(record.id))
                .join(record.user, user)
                .where(
                        set.exerciseRecord.id.eq(recordId),
                        user.userId.eq(userId)
                )
                .orderBy(set.setOrder.asc());

        System.out.println("Generated SQL: " + query.toString()); // 여기서 SQL 확인
        return query.fetch();
    }
}