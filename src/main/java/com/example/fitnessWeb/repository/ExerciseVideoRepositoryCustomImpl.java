package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseVideo;
import com.example.fitnessWeb.entity.ExerciseVideoType;
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

@RequiredArgsConstructor
public class ExerciseVideoRepositoryCustomImpl implements ExerciseVideoRepositoryCustom {

    private final EntityManager em;


    @Override
    public Page<ExerciseVideo> searchVideos(List<String> muscleGroups, List<String> exerciseNames, List<String> athleteNames, ExerciseVideoType type, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QExerciseVideo video = QExerciseVideo.exerciseVideo;
        QExerciseVideoDetail detail = QExerciseVideoDetail.exerciseVideoDetail;

        BooleanExpression condition = video.id.isNotNull();

        if (type != null) {
            condition = condition.and(video.type.eq(type));
        }
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            condition = condition.and(detail.muscleGroup.in(muscleGroups));
        }
        if (exerciseNames != null && !exerciseNames.isEmpty()) {
            condition = condition.and(detail.exerciseName.in(exerciseNames));
        }
        if (athleteNames != null && !athleteNames.isEmpty()) {
            condition = condition.and(detail.athleteName.in(athleteNames));
        }

        JPAQuery<ExerciseVideo> query = queryFactory
                .select(video)
                .from(video)
                .join(video.details, detail)
                .where(condition)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<ExerciseVideo> content = query.fetch();

        long total = queryFactory
                .select(video.countDistinct())
                .from(video)
                .join(video.details, detail)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
