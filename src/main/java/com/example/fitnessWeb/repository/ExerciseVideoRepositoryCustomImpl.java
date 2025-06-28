package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseVideo;
import com.example.fitnessWeb.entity.QExerciseVideo;
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
    public Page<ExerciseVideo> searchVideos(String muscleGroup, String athleteName, String category, Pageable pageable) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QExerciseVideo video = QExerciseVideo.exerciseVideo;

        BooleanExpression condition = null;

        if (muscleGroup != null && !muscleGroup.isEmpty()) {
            condition = video.muscleGroup.eq(muscleGroup);
        }
        if (athleteName != null && !athleteName.isEmpty()) {
            condition = condition == null ? video.athleteName.eq(athleteName) : condition.and(video.athleteName.eq(athleteName));
        }
        if (category != null && !category.isEmpty()) {
            condition = condition == null ? video.category.eq(category) : condition.and(video.category.eq(category));
        }

        JPAQuery<ExerciseVideo> query = queryFactory
                .selectFrom(video)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<ExerciseVideo> content = query.fetch();

        long total = queryFactory
                .selectFrom(video)
                .where(condition)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}
