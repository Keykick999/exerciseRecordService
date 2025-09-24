package com.example.fitnessWeb.exerciseVideo.repository;

import com.example.fitnessWeb.exerciseVideo.domain.ExerciseVideo;
import com.example.fitnessWeb.exerciseVideo.enums.ExerciseVideoType;
import com.example.fitnessWeb.entity.QExerciseVideo;
import com.example.fitnessWeb.entity.QExerciseVideoDetail;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ExerciseVideoRepositoryCustomImpl implements ExerciseVideoRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ExerciseVideo> searchVideos(List<String> muscleGroups, List<String> exerciseNames, List<String> athleteNames, ExerciseVideoType type, Pageable pageable) {
        // JPAQueryFactory 생성
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QExerciseVideo video = QExerciseVideo.exerciseVideo;
        QExerciseVideoDetail detail = QExerciseVideoDetail.exerciseVideoDetail;

        // 조건 생성
        BooleanExpression condition = createBaseCondition(muscleGroups, exerciseNames, athleteNames, type, video, detail);

        // 쿼리 빌드
        JPAQuery<ExerciseVideo> query = queryFactory
                .selectFrom(video)
                .join(video.details, detail).fetchJoin()  // fetchJoin을 사용하여 N+1 문제 해결
                .where(condition)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        System.out.println("Generated SQL: " + query.toString()); // 여기서 SQL 확인

        List<ExerciseVideo> content = query.fetch();

        // 총 갯수 계산
        Long total = Optional.ofNullable(queryFactory
                        .select(video.countDistinct())
                        .from(video)
                        .join(video.details, detail)
                        .where(condition)
                        .fetchOne())
                .orElse(0L);


        return new PageImpl<>(content, pageable, total);
    }

    // 조건을 생성하는 메서드
    private BooleanExpression createBaseCondition(List<String> muscleGroups, List<String> exerciseNames, List<String> athleteNames, ExerciseVideoType type, QExerciseVideo video, QExerciseVideoDetail detail) {
        BooleanExpression condition = video.id.isNotNull();  // 기본 조건

        // type 필터링
        if (type != null) {
            condition = condition.and(video.type.eq(type));
        }

        // muscleGroups 필터링
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            condition = condition.and(detail.muscleGroup.in(muscleGroups));
        }

        // exerciseNames 필터링
        if (exerciseNames != null && !exerciseNames.isEmpty()) {
            condition = condition.and(detail.exerciseName.in(exerciseNames));
        }

        // athleteNames 필터링
        if (athleteNames != null && !athleteNames.isEmpty()) {
            condition = condition.and(detail.athleteName.in(athleteNames));
        }

        return condition;
    }
}
