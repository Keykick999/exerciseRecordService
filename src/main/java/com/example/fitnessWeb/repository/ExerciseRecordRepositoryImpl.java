package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.user.entity.QUser;
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

        List<Long> recordIds = queryFactory
                .select(record.id)
                .distinct()
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(
                        record.user.userId.eq(userId),
                        startDate != null ? record.createdAt.goe(startDate) : null,
                        endDate != null ? record.createdAt.loe(endDate) : null,
                        exerciseName != null ? type.name.contains(exerciseName) : null,
                        (muscleGroupList != null && !muscleGroupList.isEmpty())
                                ? m.name.in(muscleGroupList)
                                : null
                )
                .orderBy(getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (recordIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Tuple> tuples = queryFactory
                .select(record.id, record.createdAt, m.name)
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(record.id.in(recordIds))
                .fetch();

        Map<Long, ExerciseRecordResponseDto> grouped = new LinkedHashMap<>();
        for (Tuple tuple : tuples) {
            Long recordId = tuple.get(record.id);
            LocalDateTime createdAt = tuple.get(record.createdAt);
            String muscle = tuple.get(m.name);
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

        Long total = queryFactory
                .select(record.id.countDistinct())
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(
                        record.user.userId.eq(userId),
                        startDate != null ? record.createdAt.goe(startDate) : null,
                        endDate != null ? record.createdAt.loe(endDate) : null,
                        exerciseName != null ? type.name.contains(exerciseName) : null,
                        (muscleGroupList != null && !muscleGroupList.isEmpty())
                                ? m.name.in(muscleGroupList)
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
    public List<ExerciseSetDetailDto> findExerciseSetDetails(Long recordId, String userId) {
        QExerciseSet set = QExerciseSet.exerciseSet;
        QExerciseType type = QExerciseType.exerciseType;
        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        QUser user = QUser.user;

        return queryFactory
                .select(
                        com.querydsl.core.types.Projections.constructor(
                                ExerciseSetDetailDto.class,
                                type.name,
                                set.setOrder,
                                set.weight,
                                set.reps
                        )
                )
                .from(set)
                .join(set.exerciseType, type)
                .join(record).on(set.exerciseRecord.id.eq(record.id))
                .join(record.user, user)
                .where(
                        set.exerciseRecord.id.eq(recordId),
                        user.userId.eq(userId)
                )
                .orderBy(set.setOrder.asc())
                .fetch();
    }
}
