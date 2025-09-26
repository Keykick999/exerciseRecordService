package com.example.fitnessWeb.analytics.repository;

import com.example.fitnessWeb.analytics.projection.MaxWeightRepsRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnalyticsNativeRepositoryImpl implements AnalyticsNativeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<MaxWeightRepsRow> findMaxWeightRepsHistory(
            String userId,
            Long exerciseTypeId,
            int limit,
            int offset
    ) {
        String sql = """
            select *
            from (
                select 
                    er.id as record_id,
                    er.created_at,
                    es.weight,
                    es.reps,
                    row_number() over (
                        partition by er.id
                        order by es.weight desc, es.reps desc, es.id asc
                    ) as rn
                from exercise_record er
                join exercise_set es on er.id = es.exercise_record_id
                where er.user_user_id = :userId
                  and es.exercise_type_id = :exerciseTypeId
            ) t
            where rn = 1
            order by created_at asc, record_id asc
            limit :limit offset :offset
        """;


        Query nativeQuery = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("exerciseTypeId", exerciseTypeId)
                .setParameter("limit", limit)
                .setParameter("offset", offset);

        List<Object[]> rows = nativeQuery.getResultList();
        List<MaxWeightRepsRow> results = new ArrayList<>();

        for (Object[] row : rows) {
            Long recordId = ((Number) row[0]).longValue();
            LocalDateTime createdAt = row[1] instanceof Timestamp
                    ? ((Timestamp) row[1]).toLocalDateTime()
                    : (LocalDateTime) row[1];
            Double weight = row[2] != null ? ((Number) row[2]).doubleValue() : null;
            Integer reps = row[3] != null ? ((Number) row[3]).intValue() : null;

            results.add(new MaxWeightRepsRow(recordId, createdAt, weight, reps));
        }

        return results;
    }
}
