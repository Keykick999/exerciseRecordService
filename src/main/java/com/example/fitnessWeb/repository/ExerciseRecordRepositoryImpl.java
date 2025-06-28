package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.entity.QExerciseRecord;
import com.example.fitnessWeb.entity.QExerciseSet;
import com.example.fitnessWeb.entity.QExerciseType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExerciseRecordRepositoryImpl implements ExerciseRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExerciseRecordResponseDto> searchRecords(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String exerciseName,
            List<String> muscleGroupList,
            Pageable pageable) {

        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        QExerciseSet set = QExerciseSet.exerciseSet;
        QExerciseType type = QExerciseType.exerciseType;

        // 1. distinct record.id만 페이징으로 추출
        List<Long> recordIds = queryFactory
                .select(record.id)
                .distinct()
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .where(
                        record.user.id.eq(userId),
                        startDate != null ? record.createdAt.goe(startDate) : null,
                        endDate != null ? record.createdAt.loe(endDate) : null,
                        exerciseName != null ? type.name.contains(exerciseName) : null,
                        (muscleGroupList != null && !muscleGroupList.isEmpty())
                                ? type.muscleGroup.in(muscleGroupList)
                                : null
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (recordIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. 실제 recordId 기준으로 muscleGroup 포함 전체 조회
        List<Tuple> tuples = queryFactory
                .select(record.id, record.createdAt, type.muscleGroup)
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .where(record.id.in(recordIds))
                .fetch();

        // 3. 그룹핑
        Map<Long, ExerciseRecordResponseDto> grouped = new LinkedHashMap<>();
        for (Tuple tuple : tuples) {
            Long recordId = tuple.get(record.id);
            LocalDateTime createdAt = tuple.get(record.createdAt);
            String muscle = tuple.get(type.muscleGroup);
            grouped.computeIfAbsent(recordId, id -> new ExerciseRecordResponseDto(id, createdAt))
                    .getMuscleGroupSet().add(muscle);
        }

        List<ExerciseRecordResponseDto> content = grouped.values().stream()
                .map(dto -> new ExerciseRecordResponseDto(
                        dto.getCreatedAt(),
                        String.join(", ", dto.getMuscleGroupSet()),
                        dto.getExerciseRecordId()
                ))
                .collect(Collectors.toList());

        // 4. 전체 count
        Long total = queryFactory
                .select(record.id.countDistinct())
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .where(
                        record.user.id.eq(userId),
                        startDate != null ? record.createdAt.goe(startDate) : null,
                        endDate != null ? record.createdAt.loe(endDate) : null,
                        exerciseName != null ? type.name.contains(exerciseName) : null,
                        (muscleGroupList != null && !muscleGroupList.isEmpty())
                                ? type.muscleGroup.in(muscleGroupList)
                                : null
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        PathBuilder<?> path = new PathBuilder<>(QExerciseRecord.exerciseRecord.getType(), QExerciseRecord.exerciseRecord.getMetadata());

        return pageable.getSort().stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    return new OrderSpecifier(direction, path.get(order.getProperty()));
                })
                .toArray(OrderSpecifier[]::new);
    }

    @Override
    public List<ExerciseSetDetailDto> findExerciseSetDetails(Long recordId) {
        QExerciseSet s = QExerciseSet.exerciseSet;
        QExerciseType t = QExerciseType.exerciseType;

        return queryFactory
                .select(
                        com.querydsl.core.types.Projections.constructor(
                                ExerciseSetDetailDto.class,
                                t.name,
                                s.setOrder,
                                s.weight,
                                s.reps
                        )
                )
                .from(s)
                .join(t).on(s.exerciseType.id.eq(t.id))
                .where(s.exerciseRecord.id.eq(recordId))
//                .orderBy(t.name.asc(), s.setOrder.asc())  // 이미 정렬된 상태로 저장되어 있어 주석 처리함
                .fetch();
    }
}
