package com.example.fitnessWeb.analytics.repository;

import com.example.fitnessWeb.analytics.dto.ExerciseTypeVolumeDto;
import com.example.fitnessWeb.analytics.projection.MaxRepsRow;
import com.example.fitnessWeb.analytics.projection.MaxVolumeSetRow;
import com.example.fitnessWeb.analytics.dto.MuscleBalanceDto;
import com.example.fitnessWeb.exerciseRecord.domain.ExerciseSet;
import com.example.fitnessWeb.exerciseRecord.domain.QExerciseRecord;
import com.example.fitnessWeb.exerciseRecord.domain.QExerciseSet;
import com.example.fitnessWeb.exerciseRecord.dto.WeeklyVolumeDto;
import com.example.fitnessWeb.exerciseType.domain.QExerciseMuscle;
import com.example.fitnessWeb.exerciseType.domain.QExerciseType;
import com.example.fitnessWeb.exerciseType.domain.QMuscle;
import com.example.fitnessWeb.user.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private final JPAQueryFactory queryFactory;

    private final QExerciseRecord er = QExerciseRecord.exerciseRecord;
    private final QExerciseSet es = QExerciseSet.exerciseSet;
    private final QExerciseType et = QExerciseType.exerciseType;
    private final QExerciseMuscle em = QExerciseMuscle.exerciseMuscle;
    private final QMuscle m = QMuscle.muscle;
    private final QUser u = QUser.user;


    public List<WeeklyVolumeDto> findWeeklyVolumeForUserAndRange(String userId, LocalDate start, LocalDate end) {
        WeekFields weekFields = WeekFields.ISO;
        int startYW = start.get(weekFields.weekBasedYear()) * 100 + start.get(weekFields.weekOfWeekBasedYear());
        int endYW   = end.get(weekFields.weekBasedYear())   * 100 + end.get(weekFields.weekOfWeekBasedYear());

        NumberTemplate<Integer> yearWeekExpr = Expressions.numberTemplate(
                Integer.class,
                "FUNCTION('YEARWEEK',{0},1)", er.createdAt
        );

        return queryFactory
                .select(Projections.constructor(
                        WeeklyVolumeDto.class,
                        yearWeekExpr,
                        es.weight.multiply(es.reps).sum()
                ))
                .from(er)
                .join(er.sets, es)
                .join(er.user, u)
                .where(
                        u.userId.eq(userId)
                                .and(yearWeekExpr.between(startYW, endYW))
                )
                .groupBy(yearWeekExpr)
                .orderBy(yearWeekExpr.asc())
                .fetch();
    }

    /** 근육별 볼륨 비율 조회 */
    public List<MuscleBalanceDto> findMuscleBalance(String userId, LocalDateTime start, LocalDateTime end) {

        List<MuscleBalanceDto> volumes = queryFactory
                .select(Projections.bean(MuscleBalanceDto.class,
                        m.name.as("muscle"),
                        es.weight.multiply(es.reps).sum().as("volume")
                ))
                .from(er)
                .join(er.sets, es)
                .join(es.exerciseType, et)
                .join(et.exerciseMuscles, em)
                .join(em.muscle, m)
                .join(er.user, u)
                .where(u.userId.eq(userId)
                        .and(er.createdAt.goe(start))
                        .and(er.createdAt.lt(end)))
                .groupBy(m.name)
                .fetch();

        // 전체 볼륨
        double totalVolume = volumes.stream()
                .mapToDouble(v -> v.getVolume() == null ? 0L : v.getVolume())
                .sum();

        // pct / totalVolume 후처리
        volumes.forEach(v -> {
            v.setTotalVolume(totalVolume);
            double volume = v.getVolume() == null ? 0L : v.getVolume();
            v.setPct(totalVolume > 0
                    ? Math.round(1000.0 * volume / totalVolume) / 10.0
                    : 0.0);
        });

        return volumes;
    }



    /** 특정 중량에서 최대 반복수 최근 기록 조회 */
    public List<MaxRepsRow> findRecentMaxRepsByWeight(String userId, Long exerciseTypeId, Double weight, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(MaxRepsRow.class,
                        er.id,
                        er.createdAt,
                        es.reps.max()
                ))
                .from(er)
                .join(er.sets, es)
                .where(er.user.userId.eq(userId)
                        .and(es.exerciseType.id.eq(exerciseTypeId))
                        .and(es.weight.eq(weight))
                )
                .groupBy(er.id, er.createdAt)
                .orderBy(er.createdAt.asc(), er.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }


    /** 세트 볼륨 최대값 조회 */
    public List<MaxVolumeSetRow> findMaxVolumeSets(String userId, Long exerciseTypeId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        return queryFactory
                .select(Projections.constructor(MaxVolumeSetRow.class,
                        er.id,
                        er.createdAt,
                        es.weight.multiply(es.reps),
                        es.weight,
                        es.reps
                ))
                .from(er)
                .join(er.sets, es)
                .where(er.user.userId.eq(userId)
                        .and(startDate != null ? er.createdAt.goe(startDate) : null)
                        .and(endDate != null ? er.createdAt.lt(endDate) : null)
                        .and(exerciseTypeId != null ? es.exerciseType.id.eq(exerciseTypeId) : null)
                )
                .orderBy(es.weight.multiply(es.reps).desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }


    public ExerciseTypeVolumeDto findExerciseTypeVolume(
            String userId,
            Long exerciseTypeId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        // 조건에 맞는 세트 조회
        List<ExerciseSet> sets = queryFactory
                .selectFrom(es)
                .join(es.exerciseRecord, er)
                .where(
                        er.user.userId.eq(userId)
                                .and(exerciseTypeId != null ? es.exerciseType.id.eq(exerciseTypeId) : null)
                                .and(startDate != null ? er.createdAt.goe(startDate) : null)
                                .and(endDate != null ? er.createdAt.lt(endDate) : null)
                )
                .fetch();

        // 전체 볼륨 계산
        double totalVolume = sets.stream()
                .mapToDouble(s -> s.getWeight() * s.getReps())
                .sum();

        // 무게별 볼륨 계산
        List<ExerciseTypeVolumeDto.WeightVolumeDto> weightVolumes = sets.stream()
                .collect(Collectors.groupingBy(
                        ExerciseSet::getWeight,
                        Collectors.summingDouble(s -> s.getWeight() * s.getReps())
                ))
                .entrySet().stream()
                .map(e -> new ExerciseTypeVolumeDto.WeightVolumeDto(
                        e.getKey(),
                        e.getValue() / totalVolume * 100
                ))
                .collect(Collectors.toList());

        return new ExerciseTypeVolumeDto(
                exerciseTypeId,
                totalVolume,
                weightVolumes
        );
    }
}
