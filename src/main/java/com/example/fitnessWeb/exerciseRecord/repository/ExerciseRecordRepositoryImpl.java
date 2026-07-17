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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class ExerciseRecordRepositoryImpl implements ExerciseRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QExerciseRecord exerciseRecord = QExerciseRecord.exerciseRecord;
    private static final QExerciseSet exerciseSet = QExerciseSet.exerciseSet;
    private static final QExerciseType exerciseType = QExerciseType.exerciseType;
    private static final QExerciseMuscle exerciseMuscle = QExerciseMuscle.exerciseMuscle;
    private static final QMuscle muscle = QMuscle.muscle;
    private static final QUser user = QUser.user;

    @Override
    public Slice<ExerciseRecordResponseDto> searchRecords(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String exerciseName,
            List<String> muscleGroupList,
            Pageable pageable) {
        // 1. 운동 기록 조회
        List<ExerciseRecordResponseDto> exerciseRecords = fetchExerciseRecords(userId, startDate, endDate, exerciseName, muscleGroupList, pageable);

        boolean hasNextPage = checkHasNextPage(exerciseRecords, pageable.getPageSize());

        if (exerciseRecords.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, hasNextPage);
        }

        // 2. 근육 정보 조회
        List<Tuple> muscleData = fetchMuscleData(exerciseRecords);

        // 3. 메모리 조립
        buildAndSetMuscleGroups(exerciseRecords, muscleData);

        return new SliceImpl<>(exerciseRecords, pageable, hasNextPage);
    }

    /**
     * 조건에 맞는 운동 기록을 pageSize + 1개까지 조회한다.
     * 다음 페이지 존재 여부는 호출자가 판단한다.
     */
    private List<ExerciseRecordResponseDto> fetchExerciseRecords(String userId,
                                                                 LocalDateTime startDate,
                                                                 LocalDateTime endDate,
                                                                 String exerciseName,
                                                                 List<String> muscleGroupList,
                                                                 Pageable pageable) {
        boolean hasExerciseSearchCondition = hasText(exerciseName) || (!CollectionUtils.isEmpty(muscleGroupList));
        int pageSize = pageable.getPageSize();

        JPAQuery<ExerciseRecordResponseDto> exerciseRecordQuery = queryFactory
                .select(Projections.constructor(
                        ExerciseRecordResponseDto.class,
                        exerciseRecord.id,
                        exerciseRecord.modifiedAt
                ))
                .from(exerciseRecord);

        if (hasExerciseSearchCondition) {
            exerciseRecordQuery
                    .join(exerciseRecord.sets, exerciseSet)
                    .join(exerciseSet.exerciseType, exerciseType)
                    .join(exerciseType.exerciseMuscles, exerciseMuscle)
                    .join(exerciseMuscle.muscle, muscle);
        }

        exerciseRecordQuery.where(
                userIdEq(userId),
                dateBetween(startDate, endDate),
                exerciseNameEq(exerciseName),
                muscleGroupIn(muscleGroupList)
        );


        List<ExerciseRecordResponseDto> exerciseRecords = exerciseRecordQuery
                .orderBy(exerciseRecord.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageSize + 1)  // 요청한 크기보다 1개 더 가져옴 (다음 페이지 존재 여부 확인용)
                .distinct()
                .fetch();

        return exerciseRecords;
    }

    /**
     * 다음 페이지 존재 여부를 확인한다.
     * pageSize + 1번째 데이터는 다음 페이지 존재 여부 확인용이므로 제거한다.
     */
    private boolean checkHasNextPage(
            List<ExerciseRecordResponseDto> exerciseRecords,
            int pageSize
    ) {
        if (exerciseRecords.size() <= pageSize) {
            return false;
        }

        exerciseRecords.remove(pageSize);
        return true;
    }

    /**
     * 조회된 운동 기록에 포함된 근육 정보를 조회한다.
     */
    private List<Tuple> fetchMuscleData(List<ExerciseRecordResponseDto> exerciseRecords) {
        List<Long> exerciseRecordIds = exerciseRecords.stream()
                .map(ExerciseRecordResponseDto::getExerciseRecordId)
                .toList();

        return queryFactory
                .select(exerciseRecord.id, muscle.name)
                .from(exerciseRecord)
                .join(exerciseRecord.sets, exerciseSet)
                .join(exerciseSet.exerciseType, exerciseType)
                .join(exerciseType.exerciseMuscles, exerciseMuscle)
                .join(exerciseMuscle.muscle, muscle)
                .where(exerciseRecord.id.in(exerciseRecordIds))
                .distinct()
                .fetch();
    }

    /**
     * 조회한 근육 정보를 운동 기록 DTO에 그룹별로 설정한다.
     */
    private void buildAndSetMuscleGroups(
            List<ExerciseRecordResponseDto> content,
            List<Tuple> muscleData
    ) {
        Map<Long, String> muscleMap = muscleData.stream()
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(exerciseRecord.id)),
                        Collectors.mapping(
                                tuple -> tuple.get(muscle.name),
                                Collectors.joining(", ")
                        )
                ));

        content.forEach(dto -> {
            dto.setMuscleGroups(
                    muscleMap.getOrDefault(dto.getExerciseRecordId(), "")
            );
        });
    }

    @Override
    public List<ExerciseSetDetailDto> findExerciseSetDetails(Long recordId, String userId) {
        JPAQuery<ExerciseSetDetailDto> query = queryFactory
                .select(Projections.constructor(
                        ExerciseSetDetailDto.class,
                        exerciseType.name,
                        exerciseSet.setOrder,
                        exerciseSet.weight,
                        exerciseSet.reps
                ))
                .from(exerciseSet)
                .join(exerciseSet.exerciseType, exerciseType)
                .join(exerciseRecord).on(exerciseSet.exerciseRecord.id.eq(exerciseRecord.id))
                .join(exerciseRecord.user, user)
                .where(
                        exerciseSet.exerciseRecord.id.eq(recordId),
                        user.userId.eq(userId)
                )
                .orderBy(exerciseSet.setOrder.asc());

        return query.fetch();
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
        return hasText(exerciseName) ? QExerciseType.exerciseType.name.eq(exerciseName) : null;
    }

    private BooleanExpression muscleGroupIn(List<String> muscleGroupList) {
        return (muscleGroupList != null && !muscleGroupList.isEmpty()) ? QMuscle.muscle.name.in(muscleGroupList) : null;
    }
}