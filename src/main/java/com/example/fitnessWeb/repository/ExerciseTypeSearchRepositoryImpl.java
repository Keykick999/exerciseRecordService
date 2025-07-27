package com.example.fitnessWeb.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.fitnessWeb.entity.ExerciseTypeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class ExerciseTypeSearchRepositoryImpl implements ExerciseTypeSearchRepositoryCustom {

    private final ElasticsearchClient elasticsearchClient; // RestClient 사용

    @Override
    public Page<ExerciseTypeDocument> searchByNameAndMuscles(
            String name, Set<String> muscleGroups, Pageable pageable) throws IOException {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // ① 이름 검색 (기존 그대로)
        if (name != null && !name.isBlank()) {
            boolQuery.must(m -> m
                    .queryString(q -> q
                            .fields("name")
                            .query("*" + name + "*")
                    )
            );
        }

        // ② 부위 검색: terms 쿼리 사용
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            // Set<String> → List<FieldValue> 변환
            List<FieldValue> values = muscleGroups.stream()
                    .map(FieldValue::of)
                    .toList();

            boolQuery.filter(f -> f
                    .terms(t -> t
                            .field("muscleGroups.keyword")
                            .terms(ts -> ts.value(values))
                    )
            );
        }

        // ③ SearchRequest 구성 (기존 그대로)
        SearchRequest request = SearchRequest.of(s -> s
                .index("exercise-type")
                .query(boolQuery.build()._toQuery())
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );

        SearchResponse<ExerciseTypeDocument> response =
                elasticsearchClient.search(request, ExerciseTypeDocument.class);

        List<ExerciseTypeDocument> results = response.hits().hits()
                .stream().map(h -> h.source()).toList();

        return new PageImpl<>(results, pageable, response.hits().total().value());
    }
}
