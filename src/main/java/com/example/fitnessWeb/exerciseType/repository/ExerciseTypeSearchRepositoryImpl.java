package com.example.fitnessWeb.exerciseType.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.fitnessWeb.exerciseType.domain.ExerciseTypeDocument;
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

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Page<ExerciseTypeDocument> searchByNameAndMuscles(
            String name, Set<String> muscleGroups, Pageable pageable) throws IOException {

        BoolQuery boolQuery = buildBoolQuery(name, muscleGroups);
        SearchRequest request = buildSearchRequest(boolQuery, pageable);

        SearchResponse<ExerciseTypeDocument> response =
                elasticsearchClient.search(request, ExerciseTypeDocument.class);

        List<ExerciseTypeDocument> results = response.hits().hits()
                .stream()
                .map(h -> h.source())
                .toList();

        return new PageImpl<>(results, pageable, response.hits().total().value());
    }

    private BoolQuery buildBoolQuery(String name, Set<String> muscleGroups) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (name != null && !name.isBlank()) {
            boolQuery.must(m -> m
                    .queryString(q -> q
                            .fields("name")
                            .query("*" + name + "*")
                    )
            );
        }

        if (muscleGroups != null && !muscleGroups.isEmpty()) {
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

        return boolQuery.build();
    }


    private SearchRequest buildSearchRequest(BoolQuery boolQuery, Pageable pageable) {
        return SearchRequest.of(s -> s
                .index("exercise-type")
                .query(boolQuery._toQuery())
                .from((int) pageable.getOffset())
                .size(pageable.getPageSize())
        );
    }
}
