package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.dto.ExerciseSetDetailDto;
import com.example.fitnessWeb.entity.*;
import com.example.fitnessWeb.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
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
            String exerciseName,  // exerciseName을 String으로 변경
            List<String> muscleGroupList,
            Pageable pageable) {

        // QueryDSL 객체 초기화
        QExerciseRecord record = QExerciseRecord.exerciseRecord;
        QExerciseSet set = QExerciseSet.exerciseSet;
        QExerciseType type = QExerciseType.exerciseType;
        QExerciseMuscle em = QExerciseMuscle.exerciseMuscle;
        QMuscle m = QMuscle.muscle;

        // 조건 생성
        BooleanExpression condition = buildCondition(userId, startDate, endDate, exerciseName, muscleGroupList, record, set, type, em, m);

        // 1. 운동 기록과 부위를 한 번에 조회
        List<ExerciseRecordResponseDto> content = fetchExerciseRecordsWithMuscles(condition, pageable, record, set, type, em, m);

        // 2. 총 개수 조회
        Long total = fetchTotalCount(condition, record, set, type, em, m);

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 동적 조건 생성
    private BooleanExpression buildCondition(String userId, LocalDateTime startDate, LocalDateTime endDate,
                                             String exerciseName, List<String> muscleGroupList,
                                             QExerciseRecord record, QExerciseSet set, QExerciseType type,
                                             QExerciseMuscle em, QMuscle m) {

        BooleanExpression condition = record.user.userId.eq(userId);

        if (startDate != null) {
            condition = condition.and(record.createdAt.goe(startDate));
        }
        if (endDate != null) {
            condition = condition.and(record.createdAt.loe(endDate));
        }
        if (exerciseName != null && !exerciseName.isEmpty()) {
            condition = condition.and(type.name.eq(exerciseName));  // String 비교, eq 사용
        }
        if (muscleGroupList != null && !muscleGroupList.isEmpty()) {
            condition = condition.and(m.name.in(muscleGroupList));
        }

        return condition;
    }

    // 운동 기록과 부위를 한 번에 조회 (DTO로 직접 매핑)
    private List<ExerciseRecordResponseDto> fetchExerciseRecordsWithMuscles(BooleanExpression condition, Pageable pageable,
                                                                            QExerciseRecord record, QExerciseSet set,
                                                                            QExerciseType type, QExerciseMuscle em, QMuscle m) {

        List<Tuple> tuples = queryFactory
                .select(record.id, record.createdAt, m.name)
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(condition)
                .orderBy(record.createdAt.desc())  // 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 결과를 DTO로 변환 (ID별로 muscleGroupSet 처리)
        Map<Long, ExerciseRecordResponseDto> grouped = new LinkedHashMap<>();
        for (Tuple tuple : tuples) {
            Long recordId = tuple.get(record.id);
            LocalDateTime createdAt = tuple.get(record.createdAt);
            String muscle = tuple.get(m.name);

            // ID별로 부위 정보를 Set에 추가
            grouped.computeIfAbsent(recordId, id -> new ExerciseRecordResponseDto(id, createdAt))
                    .getMuscleGroupSet().add(muscle);
        }

        // DTO로 변환 (Set을 String으로 변환)
        return grouped.values().stream()
                .map(dto -> new ExerciseRecordResponseDto(
                        dto.getCreatedAt(),
                        String.join(", ", dto.getMuscleGroupSet()),  // Set을 String으로 변환 (`, `로 구분)
                        dto.getExerciseRecordId()
                ))
                .collect(Collectors.toList());
    }


    // 총 개수 조회
    private Long fetchTotalCount(BooleanExpression condition, QExerciseRecord record,
                                 QExerciseSet set, QExerciseType type,
                                 QExerciseMuscle em, QMuscle m) {

        JPAQuery<Long> query = queryFactory
                .select(record.id.countDistinct())
                .from(record)
                .join(record.sets, set)
                .join(set.exerciseType, type)
                .join(type.exerciseMuscles, em)
                .join(em.muscle, m)
                .where(condition);

        // 쿼리 출력 (실제 날아가는 SQL 확인)
        System.out.println("Generated Query for Total Count: " + query.toString());

        return query.fetchOne();
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
