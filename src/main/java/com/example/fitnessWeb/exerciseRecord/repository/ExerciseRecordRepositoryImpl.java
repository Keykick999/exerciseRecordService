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
import org.springframework.data.domain.*;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExerciseRecordRepositoryImpl implements ExerciseRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ExerciseRecordResponseDto> searchRecords(
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
        // -> 조건에 맞는 ID와 modifiedAt만 먼저 가져옴 (커버링 인덱스 효과)
        // =================================================================================
        JPAQuery<ExerciseRecordResponseDto> paginationQuery = queryFactory
                .select(Projections.constructor(ExerciseRecordResponseDto.class,
                        record.id,
                        record.modifiedAt
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

        // 요청한 크기보다 1개 더 가져옴 (다음 페이지 존재 여부 확인용)
        int pageSize = pageable.getPageSize();

        List<ExerciseRecordResponseDto> content = paginationQuery
                .orderBy(record.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageSize + 1)
                .distinct()
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageSize) {
            content.remove(pageSize);
            hasNext = true;
        }

        // 결과 없으면 바로 빈 Slice 반환
        if (content.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        // =================================================================================
        // STEP 2. 근육 정보 조회
        // (content에는 이제 딱 요청한 개수만큼만 들어있으므로 이대로 진행하면 됨)
        // =================================================================================
        List<Long> recordIds = content.stream()
                .map(ExerciseRecordResponseDto::getExerciseRecordId)
                .toList();

        List<Tuple> muscleData = queryFactory
                .select(record.id, m.name)
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(record.id.in(recordIds))
                .distinct()
                .fetch();

        // =================================================================================
        // STEP 3. 메모리 조립
        // =================================================================================
        Map<Long, String> muscleMap = muscleData.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(record.id),
                        Collectors.mapping(tuple -> tuple.get(m.name), Collectors.joining(", "))
                ));

        content.forEach(dto -> {
            String muscles = muscleMap.getOrDefault(dto.getExerciseRecordId(), "");
            dto.setMuscleGroups(muscles);
        });

        return new SliceImpl<>(content, pageable, hasNext);
    }


    // --- 조건 메서드 ---
    private BooleanExpression userIdEq(String userId) {
        return QExerciseRecord.exerciseRecord.user.userId.eq(userId);
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        if (startDate == null && endDate == null) return null;
        if (startDate != null && endDate != null) return record.modifiedAt.between(startDate, endDate);
        if (startDate != null) return record.modifiedAt.goe(startDate);
        return record.modifiedAt.loe(endDate);
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